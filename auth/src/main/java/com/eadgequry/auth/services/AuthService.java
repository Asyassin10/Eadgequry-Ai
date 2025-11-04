package com.eadgequry.auth.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.client.dto.CreateProfileRequest;
import com.eadgequry.auth.dto.ForgotPasswordRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.UpdateEmailRequest;
import com.eadgequry.auth.dto.UpdatePasswordRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.dto.VerifyEmailRequest;
import com.eadgequry.auth.event.EmailUpdatedEvent;
import com.eadgequry.auth.event.EventProducer;
import com.eadgequry.auth.event.ForgotPasswordEvent;
import com.eadgequry.auth.event.UserRegisteredEvent;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;
    private final EventProducer eventProducer;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      ProfileServiceClient profileServiceClient, EventProducer eventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileServiceClient = profileServiceClient;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Validate request
        request.validate();

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user in auth database
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProvider("local");

        // Save user
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully with ID: {}", savedUser.getId());

        // Create profile in Profile Service via Feign Client
        try {
            CreateProfileRequest profileRequest = new CreateProfileRequest(
                savedUser.getId(),
                savedUser.getName()
            );
            profileServiceClient.createProfile(profileRequest);
            logger.info("Profile created successfully for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            logger.error("Failed to create profile for user ID: {}", savedUser.getId(), e);
            // Note: In a real production system, you might want to implement compensation logic
            // or use Saga pattern for distributed transactions
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        // Publish Kafka event for email verification
        try {
            String verificationToken = UUID.randomUUID().toString();
            // TODO: Store verification token in database
            UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                verificationToken
            );
            eventProducer.publishUserRegistered(event);
        } catch (Exception e) {
            logger.error("Failed to publish UserRegisteredEvent for user ID: {}", savedUser.getId(), e);
            // Don't fail registration if notification fails
        }

        return UserResponse.fromUser(savedUser);
    }

    /**
     * Initiate forgot password process
     * Generates a reset token and publishes event for notification
     *
     * @param request ForgotPasswordRequest with email
     * @return Success message
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        request.validate();
        logger.info("Forgot password request for email: {}", request.email());

        // Find user by email
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate reset token (in production, store this in database with expiry)
        String resetToken = UUID.randomUUID().toString();

        // TODO: Store reset token in database with expiration time

        // Publish Kafka event to Notification Service
        try {
            ForgotPasswordEvent event = new ForgotPasswordEvent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                resetToken
            );
            eventProducer.publishForgotPassword(event);
            logger.info("ForgotPasswordEvent published for user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to publish ForgotPasswordEvent for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send password reset email");
        }

        return "Password reset link sent to email";
    }

    /**
     * Verify user email with token
     *
     * @param request VerifyEmailRequest with verification token
     * @return Success message
     */
    @Transactional
    public String verifyEmail(VerifyEmailRequest request) {
        request.validate();
        logger.info("Email verification request with token");

        // TODO: Validate token from database
        // For now, this is a placeholder implementation

        // In production, you would:
        // 1. Look up the token in a verification_tokens table
        // 2. Check if it's expired
        // 3. Get the associated user
        // 4. Set emailVerifiedAt timestamp

        logger.info("Email verification successful");

        return "Email verified successfully";
    }

    /**
     * Update user password
     *
     * @param userId User ID from JWT
     * @param request UpdatePasswordRequest with current and new passwords
     * @return Success message
     */
    @Transactional
    public String updatePassword(Long userId, UpdatePasswordRequest request) {
        request.validate();
        logger.info("Password update request for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        logger.info("Password updated successfully for user ID: {}", userId);

        return "Password updated successfully";
    }

    /**
     * Update user email
     *
     * @param userId User ID from JWT
     * @param request UpdateEmailRequest with new email and password
     * @return Success message
     */
    @Transactional
    public String updateEmail(Long userId, UpdateEmailRequest request) {
        request.validate();
        logger.info("Email update request for user ID: {}", userId);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify password
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Password is incorrect");
        }

        // Check if new email already exists
        if (userRepository.existsByEmail(request.newEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Store old email before updating
        String oldEmail = user.getEmail();

        // Update email and reset verification
        user.setEmail(request.newEmail());
        user.setEmailVerifiedAt(null); // Reset verification status
        userRepository.save(user);

        // Publish Kafka event to notify both old and new email addresses
        try {
            String verificationToken = UUID.randomUUID().toString();
            // TODO: Store verification token in database
            EmailUpdatedEvent event = new EmailUpdatedEvent(
                user.getId(),
                user.getName(),
                oldEmail,
                request.newEmail(),
                verificationToken
            );
            eventProducer.publishEmailUpdated(event);
            logger.info("EmailUpdatedEvent published for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to publish EmailUpdatedEvent for user ID: {}", userId, e);
            // Don't fail the email update if notification fails
        }

        logger.info("Email updated successfully for user ID: {}", userId);

        return "Email updated successfully. Please verify your new email.";
    }
}

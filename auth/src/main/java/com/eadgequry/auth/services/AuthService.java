package com.eadgequry.auth.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.client.dto.CreateProfileRequest;
import com.eadgequry.auth.dto.ForgotPasswordRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.ResetPasswordRequest;
import com.eadgequry.auth.dto.UpdateEmailRequest;
import com.eadgequry.auth.dto.UpdatePasswordRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.dto.VerifyEmailRequest;
import com.eadgequry.auth.event.EmailUpdatedEvent;
import com.eadgequry.auth.event.EventProducer;
import com.eadgequry.auth.event.ForgotPasswordEvent;
import com.eadgequry.auth.event.UserRegisteredEvent;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.model.VerificationToken;
import com.eadgequry.auth.repository.UserRepository;
import com.eadgequry.auth.repository.VerificationTokenRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;
    private final EventProducer eventProducer;
    private final VerificationTokenRepository verificationTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      ProfileServiceClient profileServiceClient, EventProducer eventProducer,
                      VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileServiceClient = profileServiceClient;
        this.eventProducer = eventProducer;
        this.verificationTokenRepository = verificationTokenRepository;
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
                request.name()
            );
            profileServiceClient.createProfile(profileRequest);
            logger.info("Profile created successfully for user ID: {}", savedUser.getId());
        } catch (Exception e) {
            logger.error("Failed to create profile for user ID: {}", savedUser.getId(), e);
            // Note: In a real production system, you might want to implement compensation logic
            // or use Saga pattern for distributed transactions
            throw new RuntimeException("Failed to create user profile: " + e.getMessage());
        }

        // Generate and store verification token
        try {
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Token expires in 24 hours

            VerificationToken token = new VerificationToken(
                savedUser.getId(),
                verificationToken,
                expiresAt
            );
            verificationTokenRepository.save(token);
            logger.info("Verification token created for user ID: {}", savedUser.getId());

            // Publish Kafka event for email verification
            UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(),
                request.name(),
                savedUser.getEmail(),
                verificationToken
            );
            eventProducer.publishUserRegistered(event);
        } catch (Exception e) {
            logger.error("Failed to create verification token or publish event for user ID: {}", savedUser.getId(), e);
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

        // Get user's name from Profile Service
        String userName = "User"; // Default fallback
        try {
            ResponseEntity<com.eadgequry.auth.client.dto.ProfileResponse> profileResponse =
                profileServiceClient.getProfile(user.getId());
            if (profileResponse.getBody() != null) {
                userName = profileResponse.getBody().name();
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch profile for user ID: {}, using default name", user.getId());
        }

        // Publish Kafka event to Notification Service
        try {
            ForgotPasswordEvent event = new ForgotPasswordEvent(
                user.getId(),
                userName,
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

        // 1. Look up the token in the verification_tokens table
        VerificationToken verificationToken = verificationTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        // 2. Check if token is expired
        if (verificationToken.isExpired()) {
            logger.warn("Verification token expired for user ID: {}", verificationToken.getUserId());
            throw new IllegalArgumentException("Verification token has expired. Please request a new one.");
        }

        // 3. Get the associated user
        User user = userRepository.findById(verificationToken.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if email is already verified
        if (user.getEmailVerifiedAt() != null) {
            logger.info("Email already verified for user ID: {}", user.getId());
            // Delete the token since it's no longer needed
            verificationTokenRepository.delete(verificationToken);
            return "Email already verified";
        }

        // 4. Set emailVerifiedAt timestamp
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. Delete the used token
        verificationTokenRepository.delete(verificationToken);

        logger.info("Email verification successful for user ID: {}", user.getId());

        return "Email verified successfully";
    }

    /**
     * Reset password with token
     *
     * @param request ResetPasswordRequest with reset token and new password
     * @return Success message
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        request.validate();
        logger.info("Password reset request with token");

        // TODO: Validate reset token from database
        // For now, this is a placeholder implementation

        // In production, you would:
        // 1. Look up the token in a password_reset_tokens table
        // 2. Check if it's expired (typically 1 hour expiry)
        // 3. Get the associated user
        // 4. Update the user's password
        // 5. Delete the used token

        // Placeholder: For testing, we'll just log the request
        logger.info("Password reset successful");

        return "Password reset successfully";
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

        // Delete any existing verification tokens for this user
        verificationTokenRepository.deleteByUserId(userId);

        // Update email and reset verification
        user.setEmail(request.newEmail());
        user.setEmailVerifiedAt(null); // Reset verification status
        userRepository.save(user);

        // Get user's name from Profile Service
        String userName = "User"; // Default fallback
        try {
            ResponseEntity<com.eadgequry.auth.client.dto.ProfileResponse> profileResponse =
                profileServiceClient.getProfile(user.getId());
            if (profileResponse.getBody() != null) {
                userName = profileResponse.getBody().name();
            }
        } catch (Exception e) {
            logger.warn("Failed to fetch profile for user ID: {}, using default name", user.getId());
        }

        // Generate and store new verification token
        try {
            String verificationToken = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // Token expires in 24 hours

            VerificationToken token = new VerificationToken(
                user.getId(),
                verificationToken,
                expiresAt
            );
            verificationTokenRepository.save(token);
            logger.info("New verification token created for user ID: {}", userId);

            // Publish Kafka event to notify both old and new email addresses
            EmailUpdatedEvent event = new EmailUpdatedEvent(
                user.getId(),
                userName,
                oldEmail,
                request.newEmail(),
                verificationToken
            );
            eventProducer.publishEmailUpdated(event);
            logger.info("EmailUpdatedEvent published for user ID: {}", userId);
        } catch (Exception e) {
            logger.error("Failed to create verification token or publish event for user ID: {}", userId, e);
            // Don't fail the email update if notification fails
        }

        logger.info("Email updated successfully for user ID: {}", userId);

        return "Email updated successfully. Please verify your new email.";
    }
}

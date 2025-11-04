package com.eadgequry.auth.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eadgequry.auth.client.ProfileServiceClient;
import com.eadgequry.auth.client.dto.CreateProfileRequest;
import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfileServiceClient profileServiceClient;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      ProfileServiceClient profileServiceClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.profileServiceClient = profileServiceClient;
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

        return UserResponse.fromUser(savedUser);
    }
}

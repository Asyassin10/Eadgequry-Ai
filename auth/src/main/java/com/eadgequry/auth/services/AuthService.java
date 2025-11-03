package com.eadgequry.auth.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eadgequry.auth.dto.RegisterRequest;
import com.eadgequry.auth.dto.UserResponse;
import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Validate request
        request.validate();

        // Check if email already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setProvider("local");

        // Save user
        User savedUser = userRepository.save(user);

        return UserResponse.fromUser(savedUser);
    }

    public boolean testPasswordEncoder(String rawPassword) {
        String encoded = passwordEncoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches(rawPassword, encoded);
        System.out.println("=== Password Encoder Test ===");
        System.out.println("Raw: " + rawPassword);
        System.out.println("Encoded: " + encoded);
        System.out.println("Matches: " + matches);

        // Test with known hash
        String knownHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matchesKnown = passwordEncoder.matches("password123", knownHash);
        System.out.println("Known hash matches 'password123': " + matchesKnown);

        return matches && matchesKnown;
    }
}

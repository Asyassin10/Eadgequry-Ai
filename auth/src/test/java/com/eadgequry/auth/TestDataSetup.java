package com.eadgequry.auth;

import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;

@TestConfiguration
public class TestDataSetup {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void setupTestData() {
        // Create test user
        User testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setProvider("local");

        userRepository.save(testUser);
    }
}

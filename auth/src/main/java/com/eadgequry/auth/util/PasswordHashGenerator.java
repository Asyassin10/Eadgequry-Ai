package com.eadgequry.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java PasswordHashGenerator <password>");
            System.exit(1);
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = args[0];
        String hashedPassword = encoder.encode(rawPassword);

        System.out.println("\n=== BCrypt Password Hash Generator ===");
        System.out.println("Raw Password: " + rawPassword);
        System.out.println("Hashed Password: " + hashedPassword);
        System.out.println("\nSQL Insert Example:");
        System.out.println("INSERT INTO users (name, email, password, provider, created_at, updated_at)");
        System.out.println("VALUES ('Test User', 'test@example.com', '" + hashedPassword + "', 'local', NOW(), NOW());");
        System.out.println("\n=== Testing Hash ===");

        // Test the well-known hash
        String knownHash = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean matches = encoder.matches("password123", knownHash);
        System.out.println("Known hash matches 'password123': " + matches);
        System.out.println("New hash matches '" + rawPassword + "': " + encoder.matches(rawPassword, hashedPassword));
    }
}

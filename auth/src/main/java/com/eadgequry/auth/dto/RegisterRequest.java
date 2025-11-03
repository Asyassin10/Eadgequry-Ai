package com.eadgequry.auth.dto;

public record RegisterRequest(
    String name,
    String email,
    String password
) {
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}

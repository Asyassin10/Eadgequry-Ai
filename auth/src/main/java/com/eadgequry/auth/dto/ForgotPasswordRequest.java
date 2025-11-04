package com.eadgequry.auth.dto;

/**
 * DTO for forgot password request
 */
public record ForgotPasswordRequest(
    String email
) {
    public void validate() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}

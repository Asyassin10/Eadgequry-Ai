package com.eadgequry.auth.dto;

/**
 * DTO for updating user email
 */
public record UpdateEmailRequest(
    String newEmail,
    String password
) {
    public void validate() {
        if (newEmail == null || newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("New email is required");
        }
        if (!newEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required for email update");
        }
    }
}

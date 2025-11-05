package com.eadgequry.auth.dto;

/**
 * DTO for password reset request
 */
public record ResetPasswordRequest(
    String token,
    String newPassword
) {
    public void validate() {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset token is required");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }
    }
}

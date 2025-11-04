package com.eadgequry.auth.dto;

/**
 * DTO for email verification request
 */
public record VerifyEmailRequest(
    String token
) {
    public void validate() {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification token is required");
        }
    }
}

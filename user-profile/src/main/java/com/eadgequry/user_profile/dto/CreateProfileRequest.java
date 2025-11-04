package com.eadgequry.user_profile.dto;

/**
 * DTO for creating user profile
 * Called by Auth Service after user registration
 */
public record CreateProfileRequest(
    Long userId,
    String name
) {
    public void validate() {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID is required and must be positive");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
    }
}

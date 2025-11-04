package com.eadgequry.user_profile.dto;

/**
 * DTO for updating user profile
 * Allows updating name, avatar_url, and bio
 */
public record UpdateProfileRequest(
    String name,
    String avatarUrl,
    String bio
) {
    public void validate() {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (bio != null && bio.length() > 5000) {
            throw new IllegalArgumentException("Bio cannot exceed 5000 characters");
        }
    }
}

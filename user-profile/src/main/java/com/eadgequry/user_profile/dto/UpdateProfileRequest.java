package com.eadgequry.user_profile.dto;

/**
 * DTO for updating user profile
 * Allows updating name, avatar_url, bio, and preferences
 */
public record UpdateProfileRequest(
    String name,
    String avatarUrl,
    String bio,
    String preferences
) {
    public void validate() {
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (bio != null && bio.length() > 5000) {
            throw new IllegalArgumentException("Bio cannot exceed 5000 characters");
        }
        // Preferences validation can be added here if needed (e.g., JSON format check)
    }
}

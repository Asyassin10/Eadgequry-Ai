package com.eadgequry.user_profile.dto;

import com.eadgequry.user_profile.model.UserProfile;
import java.time.LocalDateTime;

/**
 * DTO for user profile response
 * Returned by all profile endpoints
 */
public record ProfileResponse(
    Long id,
    Long userId,
    String name,
    String avatarUrl,
    String bio,
    String preferences,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProfileResponse fromEntity(UserProfile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getUserId(),
            profile.getName(),
            profile.getAvatarUrl(),
            profile.getBio(),
            profile.getPreferences(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}

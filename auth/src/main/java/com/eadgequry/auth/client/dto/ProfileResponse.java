package com.eadgequry.auth.client.dto;

import java.time.LocalDateTime;

/**
 * DTO for user profile response from Profile Service
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
}

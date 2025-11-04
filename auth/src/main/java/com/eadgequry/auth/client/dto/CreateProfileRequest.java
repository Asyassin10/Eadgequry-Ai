package com.eadgequry.auth.client.dto;

/**
 * DTO for creating user profile via Profile Service
 */
public record CreateProfileRequest(
    Long userId,
    String name
) {
}

package com.eadgequry.auth.dto;

import com.eadgequry.auth.model.User;

public record UserResponse(
    Long id,
    String email,
    String provider,
    String avatarUrl
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getProvider(),
            user.getAvatarUrl()
        );
    }
}

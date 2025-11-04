package com.eadgequry.auth.event;

/**
 * Event published when a user requests password reset
 */
public record ForgotPasswordEvent(
    Long userId,
    String name,
    String email,
    String resetToken
) {
}

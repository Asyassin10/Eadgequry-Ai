package com.eadgequry.notification.dto;

/**
 * Event published when a user requests password reset
 * Consumed by Notification Service to send password reset email
 */
public record ForgotPasswordEvent(
    Long userId,
    String name,
    String email,
    String resetToken
) {
}

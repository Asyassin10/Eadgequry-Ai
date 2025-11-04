package com.eadgequry.notification.dto;

/**
 * Event published when a user registers
 * Consumed by Notification Service to send welcome/verification email
 */
public record UserRegisteredEvent(
    Long userId,
    String name,
    String email,
    String verificationToken
) {
}

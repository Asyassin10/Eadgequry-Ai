package com.eadgequry.auth.event;

/**
 * Event published when a user registers
 */
public record UserRegisteredEvent(
    Long userId,
    String name,
    String email,
    String verificationToken
) {
}

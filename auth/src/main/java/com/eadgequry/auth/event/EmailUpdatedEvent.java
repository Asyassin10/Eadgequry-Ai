package com.eadgequry.auth.event;

/**
 * Event published when a user updates their email
 */
public record EmailUpdatedEvent(
    Long userId,
    String name,
    String oldEmail,
    String newEmail,
    String verificationToken
) {
}

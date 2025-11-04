package com.eadgequry.notification.dto;

/**
 * Event published when a user updates their email
 * Consumed by Notification Service to notify both old and new email addresses
 */
public record EmailUpdatedEvent(
    Long userId,
    String name,
    String oldEmail,
    String newEmail,
    String verificationToken
) {
}

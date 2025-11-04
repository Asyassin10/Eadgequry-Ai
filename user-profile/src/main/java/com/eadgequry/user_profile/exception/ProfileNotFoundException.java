package com.eadgequry.user_profile.exception;

/**
 * Exception thrown when profile is not found
 */
public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String message) {
        super(message);
    }

    public ProfileNotFoundException(Long userId) {
        super("Profile not found for user ID: " + userId);
    }
}

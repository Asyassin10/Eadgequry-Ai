package com.eadgequry.data_source_service.exception;

public class DatabaseConfigNotFoundException extends RuntimeException {
    public DatabaseConfigNotFoundException(String message) {
        super(message);
    }

    public DatabaseConfigNotFoundException(Long id) {
        super("Database configuration not found with id: " + id);
    }

    public DatabaseConfigNotFoundException(Long id, Long userId) {
        super("Database configuration not found with id: " + id + " for user: " + userId);
    }
}

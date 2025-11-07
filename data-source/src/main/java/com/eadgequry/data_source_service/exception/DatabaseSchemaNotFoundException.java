package com.eadgequry.data_source_service.exception;

public class DatabaseSchemaNotFoundException extends RuntimeException {
    public DatabaseSchemaNotFoundException(String message) {
        super(message);
    }

    public DatabaseSchemaNotFoundException(Long id) {
        super("Database schema not found with id: " + id);
    }

    public DatabaseSchemaNotFoundException(Long configId, boolean byConfigId) {
        super("Database schema not found for config id: " + configId);
    }
}

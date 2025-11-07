package com.eadgequry.chat_bot_service.exception;

public class InvalidSqlException extends RuntimeException {

    private final String violationType;

    public InvalidSqlException(String message) {
        super(message);
        this.violationType = "INVALID_SQL";
    }

    public InvalidSqlException(String message, String violationType) {
        super(message);
        this.violationType = violationType;
    }

    public String getViolationType() {
        return violationType;
    }
}

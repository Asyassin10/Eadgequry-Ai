package com.eadgequry.data_source_service.exception;

public class DatabaseConnectionFailedException extends RuntimeException {

    private final String exceptionType;
    private final String sqlState;
    private final Integer errorCode;

    public DatabaseConnectionFailedException(String message, String exceptionType, String sqlState, Integer errorCode) {
        super(message);
        this.exceptionType = exceptionType;
        this.sqlState = sqlState;
        this.errorCode = errorCode;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getSqlState() {
        return sqlState;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}

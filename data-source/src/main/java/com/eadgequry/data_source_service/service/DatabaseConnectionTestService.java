package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class DatabaseConnectionTestService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTestService.class);

    /**
     * Test database connection from CreateDatabaseConfigRequest
     */
    public ConnectionTestResult testConnection(CreateDatabaseConfigRequest request) {
        return testConnectionInternal(
                request.getType(),
                request.getHost(),
                request.getPort(),
                request.getDatabaseName(),
                request.getUsername(),
                request.getPassword()
        );
    }

    /**
     * Test database connection from existing DatabaseConfig
     */
    public ConnectionTestResult testConnection(DatabaseConfig config) {
        return testConnectionInternal(
                config.getType(),
                config.getHost(),
                config.getPort(),
                config.getDatabaseName(),
                config.getUsername(),
                config.getPassword()
        );
    }

    private ConnectionTestResult testConnectionInternal(
            String type,
            String host,
            Integer port,
            String databaseName,
            String username,
            String password
    ) {
        Connection connection = null;
        try {
            String jdbcUrl = buildJdbcUrl(type, host, port, databaseName);
            logger.info("Testing connection to: {} (type: {})", jdbcUrl, type);

            // Set connection timeout
            DriverManager.setLoginTimeout(10); // 10 seconds timeout

            connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Test if connection is valid
            if (connection.isValid(5)) {
                logger.info("Connection test successful for database: {}", databaseName);
                return ConnectionTestResult.success("Connection successful! Connected to " + databaseName);
            } else {
                logger.warn("Connection established but not valid for database: {}", databaseName);
                return ConnectionTestResult.failure("Connection established but validation failed");
            }

        } catch (SQLException e) {
            logger.error("Connection test failed for database: {}", databaseName, e);
            return ConnectionTestResult.failure(
                    "Connection failed: " + extractErrorMessage(e),
                    e.getClass().getSimpleName(),
                    e.getSQLState(),
                    e.getErrorCode()
            );
        } catch (Exception e) {
            logger.error("Unexpected error during connection test for database: {}", databaseName, e);
            return ConnectionTestResult.failure(
                    "Connection failed: " + e.getMessage(),
                    e.getClass().getSimpleName()
            );
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.warn("Error closing test connection", e);
                }
            }
        }
    }

    private String buildJdbcUrl(String type, String host, Integer port, String databaseName) {
        switch (type.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        host, port != null ? port : 3306, databaseName);

            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s",
                        host, port != null ? port : 5432, databaseName);

            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                        host, port != null ? port : 1433, databaseName);

            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s",
                        host, port != null ? port : 1521, databaseName);

            case "h2":
                return String.format("jdbc:h2:tcp://%s:%d/%s",
                        host, port != null ? port : 9092, databaseName);

            default:
                throw new IllegalArgumentException("Unsupported database type: " + type);
        }
    }

    private String extractErrorMessage(SQLException e) {
        String message = e.getMessage();

        // Common error patterns and user-friendly messages
        if (message.contains("Access denied")) {
            return "Access denied. Please check username and password.";
        } else if (message.contains("Unknown database")) {
            return "Database '" + extractDatabaseName(message) + "' does not exist.";
        } else if (message.contains("Communications link failure") || message.contains("Connection refused")) {
            return "Cannot connect to database server. Please check host and port.";
        } else if (message.contains("timeout")) {
            return "Connection timeout. Database server might be unreachable.";
        } else if (message.contains("No suitable driver")) {
            return "Database driver not found. Please contact administrator.";
        } else {
            // Return first line of error message
            return message.split("\n")[0];
        }
    }

    private String extractDatabaseName(String errorMessage) {
        // Extract database name from error message like "Unknown database 'test_db'"
        if (errorMessage.contains("'") && errorMessage.lastIndexOf("'") > errorMessage.indexOf("'")) {
            int start = errorMessage.indexOf("'") + 1;
            int end = errorMessage.lastIndexOf("'");
            return errorMessage.substring(start, end);
        }
        return "";
    }

    /**
     * Result object for connection test
     */
    public static class ConnectionTestResult {
        private final boolean success;
        private final String message;
        private final String exceptionType;
        private final String sqlState;
        private final Integer errorCode;
        private final Map<String, Object> details;

        private ConnectionTestResult(boolean success, String message, String exceptionType,
                                      String sqlState, Integer errorCode) {
            this.success = success;
            this.message = message;
            this.exceptionType = exceptionType;
            this.sqlState = sqlState;
            this.errorCode = errorCode;
            this.details = new HashMap<>();
        }

        public static ConnectionTestResult success(String message) {
            return new ConnectionTestResult(true, message, null, null, null);
        }

        public static ConnectionTestResult failure(String message) {
            return new ConnectionTestResult(false, message, null, null, null);
        }

        public static ConnectionTestResult failure(String message, String exceptionType) {
            return new ConnectionTestResult(false, message, exceptionType, null, null);
        }

        public static ConnectionTestResult failure(String message, String exceptionType,
                                                    String sqlState, Integer errorCode) {
            return new ConnectionTestResult(false, message, exceptionType, sqlState, errorCode);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
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

        public Map<String, Object> getDetails() {
            return details;
        }

        public void addDetail(String key, Object value) {
            this.details.put(key, value);
        }
    }
}

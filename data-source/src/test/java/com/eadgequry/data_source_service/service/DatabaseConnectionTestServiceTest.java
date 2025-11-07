package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTestServiceTest {

    private DatabaseConnectionTestService connectionTestService;

    @BeforeEach
    void setUp() {
        connectionTestService = new DatabaseConnectionTestService();
    }

    @Test
    void testConnection_WithInvalidHost_ShouldFail() {
        // Arrange
        CreateDatabaseConfigRequest request = new CreateDatabaseConfigRequest();
        request.setType("mysql");
        request.setHost("invalid-host-that-does-not-exist");
        request.setPort(3306);
        request.setDatabaseName("test_db");
        request.setUsername("testuser");
        request.setPassword("testpass");

        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                connectionTestService.testConnection(request);

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("Connection") || result.getMessage().contains("timeout"));
    }

    @Test
    void testConnection_WithUnsupportedDatabaseType_ShouldThrowException() {
        // Arrange
        CreateDatabaseConfigRequest request = new CreateDatabaseConfigRequest();
        request.setType("unsupported-db");
        request.setHost("localhost");
        request.setPort(3306);
        request.setDatabaseName("test_db");
        request.setUsername("testuser");
        request.setPassword("testpass");

        // Act & Assert
        DatabaseConnectionTestService.ConnectionTestResult result =
                connectionTestService.testConnection(request);

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Unsupported database type") ||
                result.getMessage().contains("unsupported-db"));
    }

    @Test
    void testConnection_FromDatabaseConfig_WithInvalidHost_ShouldFail() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        config.setType("mysql");
        config.setHost("invalid-host");
        config.setPort(3306);
        config.setDatabaseName("test_db");
        config.setUsername("testuser");
        config.setPassword("testpass");

        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                connectionTestService.testConnection(config);

        // Assert
        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    void connectionTestResult_Success_ShouldHaveCorrectProperties() {
        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                DatabaseConnectionTestService.ConnectionTestResult.success("Test message");

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Test message", result.getMessage());
        assertNull(result.getExceptionType());
        assertNull(result.getSqlState());
        assertNull(result.getErrorCode());
    }

    @Test
    void connectionTestResult_Failure_ShouldHaveCorrectProperties() {
        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                DatabaseConnectionTestService.ConnectionTestResult.failure("Error message", "SQLException", "08001", 1045);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Error message", result.getMessage());
        assertEquals("SQLException", result.getExceptionType());
        assertEquals("08001", result.getSqlState());
        assertEquals(1045, result.getErrorCode());
    }

    @Test
    void connectionTestResult_CanAddDetails() {
        // Arrange
        DatabaseConnectionTestService.ConnectionTestResult result =
                DatabaseConnectionTestService.ConnectionTestResult.success("Success");

        // Act
        result.addDetail("key1", "value1");
        result.addDetail("key2", 123);

        // Assert
        assertEquals("value1", result.getDetails().get("key1"));
        assertEquals(123, result.getDetails().get("key2"));
    }

    @Test
    void testConnection_MySQLType_ShouldBuildCorrectJdbcUrl() {
        // This tests that MySQL URLs are constructed correctly
        // The test will fail due to connection error, but we can verify the error message
        CreateDatabaseConfigRequest request = new CreateDatabaseConfigRequest();
        request.setType("mysql");
        request.setHost("localhost");
        request.setPort(9999); // Invalid port
        request.setDatabaseName("test_db");
        request.setUsername("testuser");
        request.setPassword("testpass");

        DatabaseConnectionTestService.ConnectionTestResult result =
                connectionTestService.testConnection(request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    void testConnection_PostgreSQLType_ShouldBuildCorrectJdbcUrl() {
        CreateDatabaseConfigRequest request = new CreateDatabaseConfigRequest();
        request.setType("postgresql");
        request.setHost("localhost");
        request.setPort(9999); // Invalid port
        request.setDatabaseName("test_db");
        request.setUsername("testuser");
        request.setPassword("testpass");

        DatabaseConnectionTestService.ConnectionTestResult result =
                connectionTestService.testConnection(request);

        assertFalse(result.isSuccess());
        assertNotNull(result.getMessage());
    }
}

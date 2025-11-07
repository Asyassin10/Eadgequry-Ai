package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.exception.DatabaseConnectionFailedException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseConfigServiceTest {

    @Mock
    private DatabaseConfigRepository databaseConfigRepository;

    @Mock
    private DatabaseConnectionTestService connectionTestService;

    @Mock
    private DatabaseSchemaExtractionService schemaExtractionService;

    @Mock
    private DatabaseSchemaService schemaService;

    @InjectMocks
    private DatabaseConfigService databaseConfigService;

    private DatabaseConfig testConfig;
    private CreateDatabaseConfigRequest testRequest;
    private DatabaseConnectionTestService.ConnectionTestResult successResult;
    private DatabaseConnectionTestService.ConnectionTestResult failureResult;

    @BeforeEach
    void setUp() {
        testConfig = new DatabaseConfig();
        testConfig.setId(1L);
        testConfig.setUserId(100L);
        testConfig.setName("Test DB");
        testConfig.setType("mysql");
        testConfig.setHost("localhost");
        testConfig.setPort(3306);
        testConfig.setDatabaseName("test_db");
        testConfig.setUsername("testuser");
        testConfig.setPassword("testpass");
        testConfig.setIsConnected(true);
        testConfig.setStatus("active");
        testConfig.setCreatedAt(LocalDateTime.now());
        testConfig.setUpdatedAt(LocalDateTime.now());

        testRequest = new CreateDatabaseConfigRequest();
        testRequest.setName("Test DB");
        testRequest.setType("mysql");
        testRequest.setHost("localhost");
        testRequest.setPort(3306);
        testRequest.setDatabaseName("test_db");
        testRequest.setUsername("testuser");
        testRequest.setPassword("testpass");

        successResult = DatabaseConnectionTestService.ConnectionTestResult.success("Connection successful");
        failureResult = DatabaseConnectionTestService.ConnectionTestResult.failure("Connection failed", "SQLException");
    }

    @Test
    void getAllConfigsByUser_ShouldReturnListOfConfigs() {
        // Arrange
        List<DatabaseConfig> configs = Arrays.asList(testConfig);
        when(databaseConfigRepository.findByUserId(100L)).thenReturn(configs);

        // Act
        List<DatabaseConfigDTO> result = databaseConfigService.getAllConfigsByUser(100L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test DB", result.get(0).getName());
        verify(databaseConfigRepository).findByUserId(100L);
    }

    @Test
    void getConfigById_WhenExists_ShouldReturnConfig() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testConfig));

        // Act
        DatabaseConfigDTO result = databaseConfigService.getConfigById(1L, 100L);

        // Assert
        assertNotNull(result);
        assertEquals("Test DB", result.getName());
        assertEquals("mysql", result.getType());
        verify(databaseConfigRepository).findByIdAndUserId(1L, 100L);
    }

    @Test
    void getConfigById_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseConfigNotFoundException.class, () ->
                databaseConfigService.getConfigById(1L, 100L));
        verify(databaseConfigRepository).findByIdAndUserId(1L, 100L);
    }

    @Test
    void createConfig_WhenConnectionSucceeds_ShouldSaveAndExtractSchema() {
        // Arrange
        when(connectionTestService.testConnection(any(CreateDatabaseConfigRequest.class)))
                .thenReturn(successResult);
        when(databaseConfigRepository.save(any(DatabaseConfig.class)))
                .thenReturn(testConfig);
        when(schemaExtractionService.extractSchema(any(DatabaseConfig.class)))
                .thenReturn("{\"tables\":[]}");

        // Act
        DatabaseConfigDTO result = databaseConfigService.createConfig(100L, testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test DB", result.getName());
        verify(connectionTestService).testConnection(any(CreateDatabaseConfigRequest.class));
        verify(databaseConfigRepository).save(any(DatabaseConfig.class));
        verify(schemaExtractionService).extractSchema(any(DatabaseConfig.class));
        verify(schemaService).saveOrUpdateSchema(anyLong(), anyString());
    }

    @Test
    void createConfig_WhenConnectionFails_ShouldThrowException() {
        // Arrange
        when(connectionTestService.testConnection(any(CreateDatabaseConfigRequest.class)))
                .thenReturn(failureResult);

        // Act & Assert
        assertThrows(DatabaseConnectionFailedException.class, () ->
                databaseConfigService.createConfig(100L, testRequest));
        verify(connectionTestService).testConnection(any(CreateDatabaseConfigRequest.class));
        verify(databaseConfigRepository, never()).save(any(DatabaseConfig.class));
        verify(schemaExtractionService, never()).extractSchema(any(DatabaseConfig.class));
    }

    @Test
    void createConfig_WhenSchemaExtractionFails_ShouldStillSaveConfig() {
        // Arrange
        when(connectionTestService.testConnection(any(CreateDatabaseConfigRequest.class)))
                .thenReturn(successResult);
        when(databaseConfigRepository.save(any(DatabaseConfig.class)))
                .thenReturn(testConfig);
        when(schemaExtractionService.extractSchema(any(DatabaseConfig.class)))
                .thenThrow(new RuntimeException("Schema extraction failed"));

        // Act
        DatabaseConfigDTO result = databaseConfigService.createConfig(100L, testRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Test DB", result.getName());
        verify(databaseConfigRepository).save(any(DatabaseConfig.class));
        verify(schemaExtractionService).extractSchema(any(DatabaseConfig.class));
        verify(schemaService, never()).saveOrUpdateSchema(anyLong(), anyString());
    }

    @Test
    void updateConfig_ShouldUpdateAndReExtractSchema() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testConfig));
        when(databaseConfigRepository.save(any(DatabaseConfig.class)))
                .thenReturn(testConfig);
        when(schemaExtractionService.extractSchema(any(DatabaseConfig.class)))
                .thenReturn("{\"tables\":[]}");

        // Act
        DatabaseConfigDTO result = databaseConfigService.updateConfig(1L, 100L, testRequest);

        // Assert
        assertNotNull(result);
        verify(databaseConfigRepository).findByIdAndUserId(1L, 100L);
        verify(databaseConfigRepository).save(any(DatabaseConfig.class));
        verify(schemaExtractionService).extractSchema(any(DatabaseConfig.class));
        verify(schemaService).saveOrUpdateSchema(anyLong(), anyString());
    }

    @Test
    void updateConfig_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseConfigNotFoundException.class, () ->
                databaseConfigService.updateConfig(1L, 100L, testRequest));
        verify(databaseConfigRepository).findByIdAndUserId(1L, 100L);
        verify(databaseConfigRepository, never()).save(any(DatabaseConfig.class));
    }

    @Test
    void deleteConfig_WhenExists_ShouldDelete() {
        // Arrange
        when(databaseConfigRepository.existsById(1L)).thenReturn(true);

        // Act
        databaseConfigService.deleteConfig(1L, 100L);

        // Assert
        verify(databaseConfigRepository).existsById(1L);
        verify(databaseConfigRepository).deleteByIdAndUserId(1L, 100L);
    }

    @Test
    void deleteConfig_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(databaseConfigRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(DatabaseConfigNotFoundException.class, () ->
                databaseConfigService.deleteConfig(1L, 100L));
        verify(databaseConfigRepository).existsById(1L);
        verify(databaseConfigRepository, never()).deleteByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    void updateConnectionStatus_WhenConnected_ShouldUpdateToActive() {
        // Arrange
        when(databaseConfigRepository.findById(1L)).thenReturn(Optional.of(testConfig));
        when(databaseConfigRepository.save(any(DatabaseConfig.class))).thenReturn(testConfig);

        // Act
        databaseConfigService.updateConnectionStatus(1L, true);

        // Assert
        verify(databaseConfigRepository).findById(1L);
        verify(databaseConfigRepository).save(argThat(config ->
                config.getIsConnected() && "active".equals(config.getStatus())));
    }

    @Test
    void updateConnectionStatus_WhenDisconnected_ShouldUpdateToInactive() {
        // Arrange
        when(databaseConfigRepository.findById(1L)).thenReturn(Optional.of(testConfig));
        when(databaseConfigRepository.save(any(DatabaseConfig.class))).thenReturn(testConfig);

        // Act
        databaseConfigService.updateConnectionStatus(1L, false);

        // Assert
        verify(databaseConfigRepository).findById(1L);
        verify(databaseConfigRepository).save(argThat(config ->
                !config.getIsConnected() && "inactive".equals(config.getStatus())));
    }

    @Test
    void testExistingConnection_WhenSucceeds_ShouldUpdateStatus() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testConfig));
        when(connectionTestService.testConnection(any(DatabaseConfig.class)))
                .thenReturn(successResult);
        when(databaseConfigRepository.findById(1L)).thenReturn(Optional.of(testConfig));
        when(databaseConfigRepository.save(any(DatabaseConfig.class))).thenReturn(testConfig);

        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                databaseConfigService.testExistingConnection(1L, 100L);

        // Assert
        assertTrue(result.isSuccess());
        verify(connectionTestService).testConnection(any(DatabaseConfig.class));
        verify(databaseConfigRepository).save(any(DatabaseConfig.class));
    }

    @Test
    void testExistingConnection_WhenFails_ShouldUpdateStatus() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(1L, 100L))
                .thenReturn(Optional.of(testConfig));
        when(connectionTestService.testConnection(any(DatabaseConfig.class)))
                .thenReturn(failureResult);
        when(databaseConfigRepository.findById(1L)).thenReturn(Optional.of(testConfig));
        when(databaseConfigRepository.save(any(DatabaseConfig.class))).thenReturn(testConfig);

        // Act
        DatabaseConnectionTestService.ConnectionTestResult result =
                databaseConfigService.testExistingConnection(1L, 100L);

        // Assert
        assertFalse(result.isSuccess());
        verify(connectionTestService).testConnection(any(DatabaseConfig.class));
        verify(databaseConfigRepository).save(any(DatabaseConfig.class));
    }
}

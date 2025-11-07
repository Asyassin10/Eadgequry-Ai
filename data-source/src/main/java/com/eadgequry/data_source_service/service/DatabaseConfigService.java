package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.exception.DatabaseConnectionFailedException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatabaseConfigService {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigService.class);

    private final DatabaseConfigRepository databaseConfigRepository;
    private final DatabaseConnectionTestService connectionTestService;
    private final DatabaseSchemaExtractionService schemaExtractionService;
    private final DatabaseSchemaService schemaService;

    public DatabaseConfigService(DatabaseConfigRepository databaseConfigRepository,
                                  DatabaseConnectionTestService connectionTestService,
                                  DatabaseSchemaExtractionService schemaExtractionService,
                                  DatabaseSchemaService schemaService) {
        this.databaseConfigRepository = databaseConfigRepository;
        this.connectionTestService = connectionTestService;
        this.schemaExtractionService = schemaExtractionService;
        this.schemaService = schemaService;
    }

    public List<DatabaseConfigDTO> getAllConfigsByUser(Long userId) {
        return databaseConfigRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public DatabaseConfigDTO getConfigById(Long id, Long userId) {
        DatabaseConfig config = databaseConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DatabaseConfigNotFoundException(id, userId));
        return toDTO(config);
    }

    public DatabaseConfigDTO createConfig(Long userId, CreateDatabaseConfigRequest request) {
        // Test connection before saving
        DatabaseConnectionTestService.ConnectionTestResult testResult = connectionTestService.testConnection(request);

        if (!testResult.isSuccess()) {
            throw new DatabaseConnectionFailedException(testResult.getMessage(),
                    testResult.getExceptionType(),
                    testResult.getSqlState(),
                    testResult.getErrorCode());
        }

        // Connection successful, create config
        DatabaseConfig config = new DatabaseConfig();
        config.setUserId(userId);
        mapRequestToEntity(request, config);

        // Set initial connection status
        config.setIsConnected(true);
        config.setStatus("active");
        config.setLastConnectedAt(LocalDateTime.now());

        DatabaseConfig saved = databaseConfigRepository.save(config);

        // Automatically extract and save database schema
        try {
            logger.info("Extracting schema for database config ID: {}", saved.getId());
            String schemaJson = schemaExtractionService.extractSchema(saved);
            schemaService.saveOrUpdateSchema(saved.getId(), schemaJson);
            logger.info("Schema extraction successful for database config ID: {}", saved.getId());
        } catch (Exception e) {
            logger.error("Schema extraction failed for database config ID: {}. Error: {}",
                    saved.getId(), e.getMessage(), e);
            // Don't fail the entire operation if schema extraction fails
            // The config is still saved and usable
        }

        return toDTO(saved);
    }

    public DatabaseConfigDTO updateConfig(Long id, Long userId, CreateDatabaseConfigRequest request) {
        DatabaseConfig config = databaseConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DatabaseConfigNotFoundException(id, userId));

        mapRequestToEntity(request, config);
        DatabaseConfig updated = databaseConfigRepository.save(config);

        // Re-extract schema if connection details changed
        try {
            logger.info("Re-extracting schema for updated database config ID: {}", updated.getId());
            String schemaJson = schemaExtractionService.extractSchema(updated);
            schemaService.saveOrUpdateSchema(updated.getId(), schemaJson);
            logger.info("Schema re-extraction successful for database config ID: {}", updated.getId());
        } catch (Exception e) {
            logger.warn("Schema re-extraction failed for database config ID: {}. Error: {}",
                    updated.getId(), e.getMessage());
            // Don't fail the update if schema extraction fails
        }

        return toDTO(updated);
    }

    public void deleteConfig(Long id, Long userId) {
        if (!databaseConfigRepository.existsById(id)) {
            throw new DatabaseConfigNotFoundException(id, userId);
        }
        databaseConfigRepository.deleteByIdAndUserId(id, userId);
    }

    public void updateConnectionStatus(Long id, boolean isConnected) {
        DatabaseConfig config = databaseConfigRepository.findById(id)
                .orElseThrow(() -> new DatabaseConfigNotFoundException(id));

        config.setIsConnected(isConnected);
        if (isConnected) {
            config.setLastConnectedAt(LocalDateTime.now());
            config.setStatus("active");
        } else {
            config.setStatus("inactive");
        }
        databaseConfigRepository.save(config);
    }

    /**
     * Test connection for an existing config (without saving)
     */
    public DatabaseConnectionTestService.ConnectionTestResult testExistingConnection(Long id, Long userId) {
        DatabaseConfig config = databaseConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DatabaseConfigNotFoundException(id, userId));

        DatabaseConnectionTestService.ConnectionTestResult result = connectionTestService.testConnection(config);

        // Update connection status based on test result
        updateConnectionStatus(id, result.isSuccess());

        return result;
    }

    private void mapRequestToEntity(CreateDatabaseConfigRequest request, DatabaseConfig entity) {
        entity.setName(request.getName());
        entity.setType(request.getType());
        entity.setHost(request.getHost());
        entity.setPort(request.getPort());
        entity.setDatabaseName(request.getDatabaseName());
        entity.setUsername(request.getUsername());
        entity.setPassword(request.getPassword());
        entity.setFilePath(request.getFilePath());
        entity.setServiceName(request.getServiceName());
        entity.setSid(request.getSid());
        entity.setInstanceName(request.getInstanceName());
        entity.setAccount(request.getAccount());
        entity.setWarehouse(request.getWarehouse());
        entity.setSchemaName(request.getSchemaName());
        entity.setRole(request.getRole());
        entity.setProjectId(request.getProjectId());
        entity.setDataset(request.getDataset());
        entity.setServiceAccountJson(request.getServiceAccountJson());
        entity.setConnectionProperties(request.getConnectionProperties());
    }

    private DatabaseConfigDTO toDTO(DatabaseConfig entity) {
        DatabaseConfigDTO dto = new DatabaseConfigDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setHost(entity.getHost());
        dto.setPort(entity.getPort());
        dto.setDatabaseName(entity.getDatabaseName());
        dto.setUsername(entity.getUsername());
        dto.setFilePath(entity.getFilePath());
        dto.setServiceName(entity.getServiceName());
        dto.setSid(entity.getSid());
        dto.setInstanceName(entity.getInstanceName());
        dto.setAccount(entity.getAccount());
        dto.setWarehouse(entity.getWarehouse());
        dto.setSchemaName(entity.getSchemaName());
        dto.setRole(entity.getRole());
        dto.setProjectId(entity.getProjectId());
        dto.setDataset(entity.getDataset());
        dto.setConnectionProperties(entity.getConnectionProperties());
        dto.setStatus(entity.getStatus());
        dto.setIsConnected(entity.getIsConnected());
        dto.setLastConnectedAt(entity.getLastConnectedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}

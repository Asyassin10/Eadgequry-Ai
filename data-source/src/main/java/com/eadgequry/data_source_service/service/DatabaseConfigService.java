package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DatabaseConfigService {

    private final DatabaseConfigRepository databaseConfigRepository;

    public DatabaseConfigService(DatabaseConfigRepository databaseConfigRepository) {
        this.databaseConfigRepository = databaseConfigRepository;
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
        DatabaseConfig config = new DatabaseConfig();
        config.setUserId(userId);
        mapRequestToEntity(request, config);

        DatabaseConfig saved = databaseConfigRepository.save(config);
        return toDTO(saved);
    }

    public DatabaseConfigDTO updateConfig(Long id, Long userId, CreateDatabaseConfigRequest request) {
        DatabaseConfig config = databaseConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DatabaseConfigNotFoundException(id, userId));

        mapRequestToEntity(request, config);
        DatabaseConfig updated = databaseConfigRepository.save(config);
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

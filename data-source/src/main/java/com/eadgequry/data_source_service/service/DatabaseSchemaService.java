package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.DatabaseSchemaDTO;
import com.eadgequry.data_source_service.exception.DatabaseSchemaNotFoundException;
import com.eadgequry.data_source_service.model.DatabaseSchema;
import com.eadgequry.data_source_service.repository.DatabaseSchemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DatabaseSchemaService {

    private final DatabaseSchemaRepository databaseSchemaRepository;

    public DatabaseSchemaService(DatabaseSchemaRepository databaseSchemaRepository) {
        this.databaseSchemaRepository = databaseSchemaRepository;
    }

    public DatabaseSchemaDTO getSchemaByConfigId(Long configId) {
        DatabaseSchema schema = databaseSchemaRepository.findByDatabaseConfigId(configId)
                .orElseThrow(() -> new DatabaseSchemaNotFoundException(configId, true));
        return toDTO(schema);
    }

    public DatabaseSchemaDTO saveOrUpdateSchema(Long configId, String schemaJson) {
        DatabaseSchema schema = databaseSchemaRepository.findByDatabaseConfigId(configId)
                .orElse(new DatabaseSchema());

        schema.setDatabaseConfigId(configId);
        schema.setSchemaJson(schemaJson);

        DatabaseSchema saved = databaseSchemaRepository.save(schema);
        return toDTO(saved);
    }

    public void deleteSchemaByConfigId(Long configId) {
        databaseSchemaRepository.deleteByDatabaseConfigId(configId);
    }

    private DatabaseSchemaDTO toDTO(DatabaseSchema entity) {
        DatabaseSchemaDTO dto = new DatabaseSchemaDTO();
        dto.setId(entity.getId());
        dto.setDatabaseConfigId(entity.getDatabaseConfigId());
        dto.setSchemaJson(entity.getSchemaJson());
        dto.setExtractedAt(entity.getExtractedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}

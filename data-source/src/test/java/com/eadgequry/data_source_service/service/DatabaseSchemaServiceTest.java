package com.eadgequry.data_source_service.service;

import com.eadgequry.data_source_service.dto.DatabaseSchemaDTO;
import com.eadgequry.data_source_service.exception.DatabaseConfigNotFoundException;
import com.eadgequry.data_source_service.exception.DatabaseSchemaNotFoundException;
import com.eadgequry.data_source_service.model.DatabaseConfig;
import com.eadgequry.data_source_service.model.DatabaseSchema;
import com.eadgequry.data_source_service.repository.DatabaseConfigRepository;
import com.eadgequry.data_source_service.repository.DatabaseSchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatabaseSchemaServiceTest {

    @Mock
    private DatabaseSchemaRepository databaseSchemaRepository;

    @Mock
    private DatabaseConfigRepository databaseConfigRepository;

    @InjectMocks
    private DatabaseSchemaService databaseSchemaService;

    private DatabaseSchema testSchema;
    private String testSchemaJson;

    @BeforeEach
    void setUp() {
        testSchemaJson = "{\"databaseName\":\"test_db\",\"tables\":[{\"name\":\"users\",\"columns\":[{\"name\":\"id\",\"type\":\"INT\"}]}]}";

        testSchema = new DatabaseSchema();
        testSchema.setId(1L);
        testSchema.setDatabaseConfigId(100L);
        testSchema.setSchemaJson(testSchemaJson);
        testSchema.setExtractedAt(LocalDateTime.now());
        testSchema.setCreatedAt(LocalDateTime.now());
        testSchema.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void getSchemaByConfigId_WhenExists_ShouldReturnSchema() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        config.setId(100L);
        config.setUserId(1L);

        when(databaseConfigRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(config));
        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.of(testSchema));

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.getSchemaByConfigId(100L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getDatabaseConfigId());
        assertEquals(testSchemaJson, result.getSchemaJson());
        assertNotNull(result.getExtractedAt());
        verify(databaseConfigRepository).findByIdAndUserId(100L, 1L);
        verify(databaseSchemaRepository).findByDatabaseConfigId(100L);
    }

    @Test
    void getSchemaByConfigId_WhenConfigNotOwnedByUser_ShouldThrowException() {
        // Arrange
        when(databaseConfigRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseConfigNotFoundException.class, () ->
                databaseSchemaService.getSchemaByConfigId(100L, 1L));
        verify(databaseConfigRepository).findByIdAndUserId(100L, 1L);
    }

    @Test
    void getSchemaByConfigId_WhenSchemaNotExists_ShouldThrowException() {
        // Arrange
        DatabaseConfig config = new DatabaseConfig();
        config.setId(100L);
        config.setUserId(1L);

        when(databaseConfigRepository.findByIdAndUserId(100L, 1L))
                .thenReturn(Optional.of(config));
        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseSchemaNotFoundException.class, () ->
                databaseSchemaService.getSchemaByConfigId(100L, 1L));
        verify(databaseSchemaRepository).findByDatabaseConfigId(100L);
    }

    @Test
    void getSchemaByConfigIdInternal_WhenExists_ShouldReturnSchema() {
        // Arrange
        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.of(testSchema));

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.getSchemaByConfigIdInternal(100L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getDatabaseConfigId());
        assertEquals(testSchemaJson, result.getSchemaJson());
        assertNotNull(result.getExtractedAt());
        verify(databaseSchemaRepository).findByDatabaseConfigId(100L);
    }

    @Test
    void getSchemaByConfigIdInternal_WhenNotExists_ShouldThrowException() {
        // Arrange
        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DatabaseSchemaNotFoundException.class, () ->
                databaseSchemaService.getSchemaByConfigIdInternal(100L));
        verify(databaseSchemaRepository).findByDatabaseConfigId(100L);
    }

    @Test
    void saveOrUpdateSchema_WhenSchemaExists_ShouldUpdateSchema() {
        // Arrange
        String newSchemaJson = "{\"databaseName\":\"updated_db\",\"tables\":[]}";
        DatabaseSchema updatedSchema = new DatabaseSchema();
        updatedSchema.setId(1L);
        updatedSchema.setDatabaseConfigId(100L);
        updatedSchema.setSchemaJson(newSchemaJson);
        updatedSchema.setExtractedAt(LocalDateTime.now());
        updatedSchema.setCreatedAt(LocalDateTime.now());
        updatedSchema.setUpdatedAt(LocalDateTime.now());

        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.of(testSchema));
        when(databaseSchemaRepository.save(any(DatabaseSchema.class)))
                .thenReturn(updatedSchema);

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.saveOrUpdateSchema(100L, newSchemaJson);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getDatabaseConfigId());
        assertEquals(newSchemaJson, result.getSchemaJson());
        verify(databaseSchemaRepository).findByDatabaseConfigId(100L);
        verify(databaseSchemaRepository).save(argThat(schema ->
                schema.getDatabaseConfigId().equals(100L) &&
                schema.getSchemaJson().equals(newSchemaJson)));
    }

    @Test
    void saveOrUpdateSchema_WhenSchemaDoesNotExist_ShouldCreateNewSchema() {
        // Arrange
        String newSchemaJson = "{\"databaseName\":\"new_db\",\"tables\":[]}";
        DatabaseSchema newSchema = new DatabaseSchema();
        newSchema.setId(2L);
        newSchema.setDatabaseConfigId(200L);
        newSchema.setSchemaJson(newSchemaJson);
        newSchema.setExtractedAt(LocalDateTime.now());
        newSchema.setCreatedAt(LocalDateTime.now());
        newSchema.setUpdatedAt(LocalDateTime.now());

        when(databaseSchemaRepository.findByDatabaseConfigId(200L))
                .thenReturn(Optional.empty());
        when(databaseSchemaRepository.save(any(DatabaseSchema.class)))
                .thenReturn(newSchema);

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.saveOrUpdateSchema(200L, newSchemaJson);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.getDatabaseConfigId());
        assertEquals(newSchemaJson, result.getSchemaJson());
        verify(databaseSchemaRepository).findByDatabaseConfigId(200L);
        verify(databaseSchemaRepository).save(argThat(schema ->
                schema.getDatabaseConfigId().equals(200L) &&
                schema.getSchemaJson().equals(newSchemaJson)));
    }

    @Test
    void deleteSchemaByConfigId_ShouldCallRepositoryDelete() {
        // Act
        databaseSchemaService.deleteSchemaByConfigId(100L);

        // Assert
        verify(databaseSchemaRepository).deleteByDatabaseConfigId(100L);
    }

    @Test
    void saveOrUpdateSchema_ShouldSetCorrectConfigId() {
        // Arrange
        when(databaseSchemaRepository.findByDatabaseConfigId(300L))
                .thenReturn(Optional.empty());
        when(databaseSchemaRepository.save(any(DatabaseSchema.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.saveOrUpdateSchema(300L, testSchemaJson);

        // Assert
        assertEquals(300L, result.getDatabaseConfigId());
        verify(databaseSchemaRepository).save(argThat(schema ->
                schema.getDatabaseConfigId().equals(300L)));
    }

    @Test
    void toDTO_ShouldMapAllFields() {
        // Arrange
        when(databaseSchemaRepository.findByDatabaseConfigId(100L))
                .thenReturn(Optional.of(testSchema));

        // Act
        DatabaseSchemaDTO result = databaseSchemaService.getSchemaByConfigIdInternal(100L);

        // Assert
        assertNotNull(result);
        assertEquals(testSchema.getId(), result.getId());
        assertEquals(testSchema.getDatabaseConfigId(), result.getDatabaseConfigId());
        assertEquals(testSchema.getSchemaJson(), result.getSchemaJson());
        assertEquals(testSchema.getExtractedAt(), result.getExtractedAt());
        assertEquals(testSchema.getCreatedAt(), result.getCreatedAt());
        assertEquals(testSchema.getUpdatedAt(), result.getUpdatedAt());
    }
}

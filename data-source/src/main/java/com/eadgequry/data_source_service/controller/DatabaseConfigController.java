package com.eadgequry.data_source_service.controller;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.service.DatabaseConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datasource/configs")
public class DatabaseConfigController {

    private final DatabaseConfigService databaseConfigService;

    public DatabaseConfigController(DatabaseConfigService databaseConfigService) {
        this.databaseConfigService = databaseConfigService;
    }

    /**
     * Get all database configurations for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DatabaseConfigDTO>> getAllConfigs(@PathVariable Long userId) {
        List<DatabaseConfigDTO> configs = databaseConfigService.getAllConfigsByUser(userId);
        return ResponseEntity.ok(configs);
    }

    /**
     * Get a specific database configuration by ID
     */
    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> getConfigById(
            @PathVariable Long id,
            @PathVariable Long userId) {
        DatabaseConfigDTO config = databaseConfigService.getConfigById(id, userId);
        return ResponseEntity.ok(config);
    }

    /**
     * Create a new database configuration
     */
    @PostMapping("/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> createConfig(
            @PathVariable Long userId,
            @RequestBody CreateDatabaseConfigRequest request) {
        DatabaseConfigDTO created = databaseConfigService.createConfig(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing database configuration
     */
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> updateConfig(
            @PathVariable Long id,
            @PathVariable Long userId,
            @RequestBody CreateDatabaseConfigRequest request) {
        DatabaseConfigDTO updated = databaseConfigService.updateConfig(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a database configuration
     */
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Void> deleteConfig(
            @PathVariable Long id,
            @PathVariable Long userId) {
        databaseConfigService.deleteConfig(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Test database connection
     */
    @PostMapping("/{id}/test")
    public ResponseEntity<String> testConnection(@PathVariable Long id) {
        // TODO: Implement actual connection testing
        return ResponseEntity.ok("Connection test endpoint - to be implemented");
    }
}

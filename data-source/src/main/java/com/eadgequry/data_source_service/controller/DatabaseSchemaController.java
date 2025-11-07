package com.eadgequry.data_source_service.controller;

import com.eadgequry.data_source_service.dto.DatabaseSchemaDTO;
import com.eadgequry.data_source_service.service.DatabaseSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/datasource/schemas")
@Tag(name = "Database Schema", description = "APIs for retrieving database schemas")
@CrossOrigin(origins = "*")
public class DatabaseSchemaController {

    private final DatabaseSchemaService databaseSchemaService;

    public DatabaseSchemaController(DatabaseSchemaService databaseSchemaService) {
        this.databaseSchemaService = databaseSchemaService;
    }

    @Operation(summary = "Get database schema by config ID", description = "Retrieve the extracted schema for a specific database configuration")
    @GetMapping("/config/{configId}/user/{userId}")
    public ResponseEntity<DatabaseSchemaDTO> getSchemaByConfigId(
            @Parameter(description = "Database configuration ID", required = true) @PathVariable Long configId,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        
        DatabaseSchemaDTO schema = databaseSchemaService.getSchemaByConfigId(configId, userId);
        return ResponseEntity.ok(schema);
    }
}

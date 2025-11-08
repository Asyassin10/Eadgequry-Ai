package com.eadgequry.data_source_service.controller;

import com.eadgequry.data_source_service.dto.CreateDatabaseConfigRequest;
import com.eadgequry.data_source_service.dto.DatabaseConfigDTO;
import com.eadgequry.data_source_service.service.DatabaseConfigService;
import com.eadgequry.data_source_service.service.DatabaseConnectionTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/datasource/configs")
@Tag(name = "Database Configuration", description = "APIs for managing database connections and configurations")
public class DatabaseConfigController {

    private final DatabaseConfigService databaseConfigService;

    public DatabaseConfigController(DatabaseConfigService databaseConfigService) {
        this.databaseConfigService = databaseConfigService;
    }

    @Operation(summary = "Get all database configurations", description = "Retrieve all database configurations for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved configurations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatabaseConfigDTO.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DatabaseConfigDTO>> getAllConfigs(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        List<DatabaseConfigDTO> configs = databaseConfigService.getAllConfigsByUser(userId);
        return ResponseEntity.ok(configs);
    }

    @Operation(summary = "Get database configuration by ID", description = "Retrieve a specific database configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved configuration",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatabaseConfigDTO.class))),
            @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    @GetMapping("/{id}/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> getConfigById(
            @Parameter(description = "Configuration ID", required = true) @PathVariable Long id,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        DatabaseConfigDTO config = databaseConfigService.getConfigById(id, userId);
        return ResponseEntity.ok(config);
    }

    @Operation(summary = "Create database configuration", description = "Create a new database configuration with connection test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Configuration created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatabaseConfigDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or connection test failed")
    })
    @PostMapping("/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> createConfig(
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Database configuration details", required = true)
            @RequestBody CreateDatabaseConfigRequest request) {
        DatabaseConfigDTO created = databaseConfigService.createConfig(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update database configuration", description = "Update an existing database configuration and re-extract schema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DatabaseConfigDTO.class))),
            @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    @PutMapping("/{id}/user/{userId}")
    public ResponseEntity<DatabaseConfigDTO> updateConfig(
            @Parameter(description = "Configuration ID", required = true) @PathVariable Long id,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated configuration details", required = true)
            @RequestBody CreateDatabaseConfigRequest request) {
        DatabaseConfigDTO updated = databaseConfigService.updateConfig(id, userId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete database configuration", description = "Delete a database configuration (schema will be cascade deleted)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Configuration deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    @DeleteMapping("/{id}/user/{userId}")
    public ResponseEntity<Void> deleteConfig(
            @Parameter(description = "Configuration ID", required = true) @PathVariable Long id,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        databaseConfigService.deleteConfig(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Test database connection", description = "Test connection for an existing database configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Connection test completed (check 'success' field in response)"),
            @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    @PostMapping("/{id}/user/{userId}/test")
    public ResponseEntity<Map<String, Object>> testConnection(
            @Parameter(description = "Configuration ID", required = true) @PathVariable Long id,
            @Parameter(description = "User ID", required = true) @PathVariable Long userId) {
        DatabaseConnectionTestService.ConnectionTestResult result = databaseConfigService.testExistingConnection(id, userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());

        if (!result.isSuccess()) {
            if (result.getExceptionType() != null) {
                response.put("exceptionType", result.getExceptionType());
            }
            if (result.getSqlState() != null) {
                response.put("sqlState", result.getSqlState());
            }
            if (result.getErrorCode() != null) {
                response.put("errorCode", result.getErrorCode());
            }
        }

        return ResponseEntity.ok(response);
    }
}

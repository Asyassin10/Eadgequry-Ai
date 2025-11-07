package com.eadgequry.data_source_service.controller;

import com.eadgequry.data_source_service.dto.QueryExecutionRequest;
import com.eadgequry.data_source_service.dto.QueryExecutionResponse;
import com.eadgequry.data_source_service.service.QueryExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/datasource/query")
@Tag(name = "Query Execution", description = "Execute SQL queries on configured databases")
@RequiredArgsConstructor
public class QueryExecutionController {

    private static final Logger log = LoggerFactory.getLogger(QueryExecutionController.class);

    private final QueryExecutionService queryExecutionService;

    /**
     * Execute SQL query on a configured database
     * Note: Only SELECT queries are allowed for security
     */
    @PostMapping("/execute")
    @Operation(summary = "Execute SQL query", description = "Execute a SELECT query on the specified database configuration")
    public ResponseEntity<QueryExecutionResponse> executeQuery(
            @Parameter(description = "Database config ID") @RequestParam Long databaseConfigId,
            @Parameter(description = "User ID") @RequestParam Long userId,
            @RequestBody String sqlQuery) {

        log.info("Executing query for user {} on database config {}", userId, databaseConfigId);

        QueryExecutionResponse response = queryExecutionService.executeQuery(databaseConfigId, userId, sqlQuery);

        return ResponseEntity.ok(response);
    }

    /**
     * Validate SQL query without executing it
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate SQL query", description = "Validate SQL query syntax without executing it")
    public ResponseEntity<Boolean> validateQuery(@RequestBody String sqlQuery) {
        boolean isValid = queryExecutionService.validateQuerySyntax(sqlQuery);
        return ResponseEntity.ok(isValid);
    }
}

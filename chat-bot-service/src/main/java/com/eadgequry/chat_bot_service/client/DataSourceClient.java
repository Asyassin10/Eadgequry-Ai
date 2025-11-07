package com.eadgequry.chat_bot_service.client;

import com.eadgequry.chat_bot_service.dto.DatabaseSchemaDTO;
import com.eadgequry.chat_bot_service.dto.QueryExecutionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "data-source", path = "/api/datasource")
public interface DataSourceClient {

    /**
     * Get database schema for a specific database configuration
     */
    @GetMapping("/schemas/config/{configId}/user/{userId}")
    DatabaseSchemaDTO getSchemaByConfigId(
            @PathVariable("configId") Long configId,
            @PathVariable("userId") Long userId
    );

    /**
     * Execute SQL query on the specified database
     * Note: This endpoint should only accept SELECT queries
     */
    @PostMapping("/query/execute")
    QueryExecutionResponse executeQuery(
            @RequestParam("databaseConfigId") Long databaseConfigId,
            @RequestParam("userId") Long userId,
            @RequestBody String sqlQuery
    );
}

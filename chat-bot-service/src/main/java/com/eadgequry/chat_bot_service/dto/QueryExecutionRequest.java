package com.eadgequry.chat_bot_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryExecutionRequest {

    @NotNull(message = "Database config ID is required")
    private Long databaseConfigId;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "SQL query cannot be empty")
    private String sqlQuery;
}

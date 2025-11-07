package com.eadgequry.chat_bot_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryExecutionResponse {

    private boolean success;
    private String sqlQuery;
    private List<Map<String, Object>> result;
    private Integer rowCount;
    private Long executionTimeMs;
    private String error;
}

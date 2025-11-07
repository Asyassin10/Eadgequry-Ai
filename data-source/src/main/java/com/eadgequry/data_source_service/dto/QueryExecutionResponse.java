package com.eadgequry.data_source_service.dto;

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

    public static QueryExecutionResponse success(String sqlQuery, List<Map<String, Object>> result, Long executionTimeMs) {
        return QueryExecutionResponse.builder()
                .success(true)
                .sqlQuery(sqlQuery)
                .result(result)
                .rowCount(result != null ? result.size() : 0)
                .executionTimeMs(executionTimeMs)
                .build();
    }

    public static QueryExecutionResponse error(String sqlQuery, String error) {
        return QueryExecutionResponse.builder()
                .success(false)
                .sqlQuery(sqlQuery)
                .error(error)
                .build();
    }
}

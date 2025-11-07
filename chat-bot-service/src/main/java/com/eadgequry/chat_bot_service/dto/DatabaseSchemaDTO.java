package com.eadgequry.chat_bot_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseSchemaDTO {

    private Long id;
    private Long databaseConfigId;
    private String schemaJson;
    private LocalDateTime extractedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Parsed schema information
    private String databaseName;
    private String databaseType;
    private List<TableInfo> tables;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableInfo {
        private String name;
        private String type;
        private String remarks;
        private List<ColumnInfo> columns;
        private List<String> primaryKeys;
        private List<ForeignKeyInfo> foreignKeys;
        private List<IndexInfo> indexes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ColumnInfo {
        private String name;
        private String type;
        private Integer size;
        private Boolean nullable;
        private String defaultValue;
        private Integer ordinalPosition;
        private String remarks;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForeignKeyInfo {
        private String name;
        private String column;
        private String referencedTable;
        private String referencedColumn;
        private String updateRule;
        private String deleteRule;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndexInfo {
        private String name;
        private String column;
        private Boolean unique;
        private Integer ordinalPosition;
    }
}

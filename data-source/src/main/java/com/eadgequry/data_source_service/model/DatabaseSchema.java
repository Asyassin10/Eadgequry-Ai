package com.eadgequry.data_source_service.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "database_schema", indexes = {
        @Index(name = "idx_database_config_id", columnList = "database_config_id"),
        @Index(name = "idx_extracted_at", columnList = "extracted_at")
})
public class DatabaseSchema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "database_config_id", nullable = false)
    private Long databaseConfigId;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "database_config_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_schema_config",
                    foreignKeyDefinition = "FOREIGN KEY (database_config_id) REFERENCES database_config(id) ON DELETE CASCADE ON UPDATE CASCADE"))
    private DatabaseConfig databaseConfig;

    @Column(name = "schema_json", nullable = false, columnDefinition = "LONGTEXT")
    private String schemaJson;

    @Column(name = "extracted_at")
    private LocalDateTime extractedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (extractedAt == null) {
            extractedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDatabaseConfigId() {
        return databaseConfigId;
    }

    public void setDatabaseConfigId(Long databaseConfigId) {
        this.databaseConfigId = databaseConfigId;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public String getSchemaJson() {
        return schemaJson;
    }

    public void setSchemaJson(String schemaJson) {
        this.schemaJson = schemaJson;
    }

    public LocalDateTime getExtractedAt() {
        return extractedAt;
    }

    public void setExtractedAt(LocalDateTime extractedAt) {
        this.extractedAt = extractedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

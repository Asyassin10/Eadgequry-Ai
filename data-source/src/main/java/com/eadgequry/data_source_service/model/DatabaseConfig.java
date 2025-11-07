package com.eadgequry.data_source_service.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "database_config", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
public class DatabaseConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 50)
    private String type; // mysql, postgresql, oracle, sqlite, sqlserver, etc.

    // Common connection fields
    private String host;
    private Integer port;

    @Column(name = "database_name")
    private String databaseName;

    private String username;
    private String password;

    // SQLite specific
    @Column(name = "file_path", length = 1024)
    private String filePath;

    // Oracle specific
    @Column(name = "service_name")
    private String serviceName;
    private String sid;

    // SQL Server specific
    @Column(name = "instance_name")
    private String instanceName;

    // Snowflake specific
    private String account;
    private String warehouse;

    @Column(name = "schema_name")
    private String schemaName;
    private String role;

    // BigQuery specific
    @Column(name = "project_id")
    private String projectId;
    private String dataset;

    @Column(name = "service_account_json", columnDefinition = "TEXT")
    private String serviceAccountJson;

    // Additional connection properties
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "connection_properties", columnDefinition = "JSON")
    private Map<String, Object> connectionProperties;

    // Metadata
    @Column(length = 20)
    private String status = "active"; // active, inactive, error

    @Column(name = "is_connected")
    private Boolean isConnected = false;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // One-to-Many relationship with DatabaseSchema (cascade delete/update)
    @OneToMany(mappedBy = "databaseConfig", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DatabaseSchema> schemas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getServiceAccountJson() {
        return serviceAccountJson;
    }

    public void setServiceAccountJson(String serviceAccountJson) {
        this.serviceAccountJson = serviceAccountJson;
    }

    public Map<String, Object> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Map<String, Object> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(Boolean isConnected) {
        this.isConnected = isConnected;
    }

    public LocalDateTime getLastConnectedAt() {
        return lastConnectedAt;
    }

    public void setLastConnectedAt(LocalDateTime lastConnectedAt) {
        this.lastConnectedAt = lastConnectedAt;
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

    public List<DatabaseSchema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<DatabaseSchema> schemas) {
        this.schemas = schemas;
    }
}

-- Create database_config table
-- Stores database connection configurations for different database types
CREATE TABLE database_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'mysql, postgresql, oracle, sqlite, sqlserver, etc.',

    -- Common connection fields (nullable for databases that don't need them)
    host VARCHAR(255) NULL,
    port INT NULL,
    database_name VARCHAR(255) NULL,
    username VARCHAR(255) NULL,
    password VARCHAR(512) NULL,

    -- SQLite specific
    file_path VARCHAR(1024) NULL,

    -- Oracle specific
    service_name VARCHAR(255) NULL,
    sid VARCHAR(255) NULL,

    -- SQL Server specific
    instance_name VARCHAR(255) NULL,

    -- Snowflake specific
    account VARCHAR(255) NULL,
    warehouse VARCHAR(255) NULL,
    schema_name VARCHAR(255) NULL,
    role VARCHAR(255) NULL,

    -- BigQuery specific
    project_id VARCHAR(255) NULL,
    dataset VARCHAR(255) NULL,
    service_account_json TEXT NULL,

    -- Additional connection properties (for any custom configurations)
    connection_properties JSON NULL,

    -- Metadata
    status VARCHAR(20) DEFAULT 'active' COMMENT 'active, inactive, error',
    is_connected BOOLEAN DEFAULT FALSE,
    last_connected_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

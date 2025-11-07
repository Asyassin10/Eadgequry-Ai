-- Create database_schema table
-- Stores extracted schema information for each database configuration
CREATE TABLE database_schema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    database_config_id BIGINT NOT NULL,

    -- Schema as JSON (contains tables, columns, relationships, etc.)
    schema_json LONGTEXT NOT NULL,

    -- Metadata
    extracted_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign Key
    CONSTRAINT fk_database_schema_config
        FOREIGN KEY (database_config_id)
        REFERENCES database_config(id)
        ON DELETE CASCADE,

    -- Indexes
    INDEX idx_database_config_id (database_config_id),
    INDEX idx_extracted_at (extracted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

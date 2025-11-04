-- Create login_history table
-- Tracks all login attempts (successful and failed)

CREATE TABLE login_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    login_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NULL COMMENT 'IPv4 and IPv6 support',
    user_agent VARCHAR(500) NULL,
    login_status VARCHAR(20) NOT NULL COMMENT 'success or failed',
    failure_reason VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_user_id (user_id),
    INDEX idx_login_at (login_at),
    INDEX idx_login_status (login_status),
    INDEX idx_user_id_login_at (user_id, login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

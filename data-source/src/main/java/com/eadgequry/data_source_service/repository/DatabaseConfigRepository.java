package com.eadgequry.data_source_service.repository;

import com.eadgequry.data_source_service.model.DatabaseConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DatabaseConfigRepository extends JpaRepository<DatabaseConfig, Long> {

    /**
     * Find all database configurations for a specific user
     */
    List<DatabaseConfig> findByUserId(Long userId);

    /**
     * Find all database configurations by user and type
     */
    List<DatabaseConfig> findByUserIdAndType(Long userId, String type);

    /**
     * Find all database configurations by user and status
     */
    List<DatabaseConfig> findByUserIdAndStatus(Long userId, String status);

    /**
     * Find a specific database configuration by ID and user
     */
    Optional<DatabaseConfig> findByIdAndUserId(Long id, Long userId);

    /**
     * Check if a database config exists for a user with a specific name
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * Delete a database configuration by ID and user
     */
    void deleteByIdAndUserId(Long id, Long userId);

    /**
     * Count database configurations for a user
     */
    long countByUserId(Long userId);
}

package com.eadgequry.data_source_service.repository;

import com.eadgequry.data_source_service.model.DatabaseSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DatabaseSchemaRepository extends JpaRepository<DatabaseSchema, Long> {

    /**
     * Find schema by database config ID
     */
    Optional<DatabaseSchema> findByDatabaseConfigId(Long databaseConfigId);

    /**
     * Delete schema by database config ID
     */
    void deleteByDatabaseConfigId(Long databaseConfigId);

    /**
     * Check if schema exists for a database config
     */
    boolean existsByDatabaseConfigId(Long databaseConfigId);
}

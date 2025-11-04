package com.eadgequry.user_profile.repository;

import com.eadgequry.user_profile.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for UserProfile entity
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * Find profile by user_id (from Auth Service)
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Check if profile exists for user_id
     */
    boolean existsByUserId(Long userId);
}

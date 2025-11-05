package com.eadgequry.auth.repository;

import com.eadgequry.auth.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Find verification token by token string
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Find verification token by user ID
     */
    Optional<VerificationToken> findByUserId(Long userId);

    /**
     * Delete all tokens for a specific user
     */
    void deleteByUserId(Long userId);

    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);

    /**
     * Check if token exists for user
     */
    boolean existsByUserId(Long userId);
}

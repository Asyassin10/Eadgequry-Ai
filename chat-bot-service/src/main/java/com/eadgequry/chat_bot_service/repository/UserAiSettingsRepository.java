package com.eadgequry.chat_bot_service.repository;

import com.eadgequry.chat_bot_service.model.UserAiSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAiSettingsRepository extends JpaRepository<UserAiSettings, Long> {

    Optional<UserAiSettings> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}

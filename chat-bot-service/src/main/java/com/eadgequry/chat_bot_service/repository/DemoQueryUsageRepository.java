package com.eadgequry.chat_bot_service.repository;

import com.eadgequry.chat_bot_service.model.DemoQueryUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DemoQueryUsageRepository extends JpaRepository<DemoQueryUsage, Long> {

    Optional<DemoQueryUsage> findByUserIdAndUsageDate(Long userId, LocalDate usageDate);
}

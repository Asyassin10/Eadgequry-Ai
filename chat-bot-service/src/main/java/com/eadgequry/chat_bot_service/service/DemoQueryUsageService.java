package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.model.DemoQueryUsage;
import com.eadgequry.chat_bot_service.repository.DemoQueryUsageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DemoQueryUsageService {

    private static final Logger log = LoggerFactory.getLogger(DemoQueryUsageService.class);
    private static final int DAILY_QUERY_LIMIT = 10;

    private final DemoQueryUsageRepository repository;

    /**
     * Check if user has exceeded their daily query limit
     * @return true if limit exceeded, false otherwise
     */
    public boolean hasExceededDailyLimit(Long userId) {
        LocalDate today = LocalDate.now();
        DemoQueryUsage usage = repository.findByUserIdAndUsageDate(userId, today)
                .orElse(null);

        if (usage == null) {
            return false; // No queries yet today
        }

        boolean exceeded = usage.getQueryCount() >= DAILY_QUERY_LIMIT;
        if (exceeded) {
            log.warn("User {} has exceeded daily query limit ({}/{})",
                    userId, usage.getQueryCount(), DAILY_QUERY_LIMIT);
        }

        return exceeded;
    }

    /**
     * Get remaining queries for today
     * @return number of queries remaining (0 if limit exceeded)
     */
    public int getRemainingQueries(Long userId) {
        LocalDate today = LocalDate.now();
        DemoQueryUsage usage = repository.findByUserIdAndUsageDate(userId, today)
                .orElse(null);

        if (usage == null) {
            return DAILY_QUERY_LIMIT; // Full limit available
        }

        int remaining = DAILY_QUERY_LIMIT - usage.getQueryCount();
        return Math.max(0, remaining);
    }

    /**
     * Increment query count for user (creates record if doesn't exist)
     */
    @Transactional
    public void incrementQueryCount(Long userId) {
        LocalDate today = LocalDate.now();

        DemoQueryUsage usage = repository.findByUserIdAndUsageDate(userId, today)
                .orElseGet(() -> DemoQueryUsage.builder()
                        .userId(userId)
                        .usageDate(today)
                        .queryCount(0)
                        .build());

        usage.setQueryCount(usage.getQueryCount() + 1);
        repository.save(usage);

        log.debug("User {} query count: {}/{}", userId, usage.getQueryCount(), DAILY_QUERY_LIMIT);
    }

    /**
     * Get current query count for today
     */
    public int getCurrentQueryCount(Long userId) {
        LocalDate today = LocalDate.now();
        DemoQueryUsage usage = repository.findByUserIdAndUsageDate(userId, today)
                .orElse(null);

        return usage != null ? usage.getQueryCount() : 0;
    }

    /**
     * Get the daily query limit
     */
    public int getDailyLimit() {
        return DAILY_QUERY_LIMIT;
    }
}

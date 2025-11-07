package com.eadgequry.chat_bot_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Simple service to clean AI-generated SQL queries
 * Security validation is done in datasource service
 */
@Service
public class SqlValidatorService {

    private static final Logger log = LoggerFactory.getLogger(SqlValidatorService.class);

    /**
     * Clean SQL query from AI response (remove markdown, extra quotes, etc.)
     */
    public String cleanQuery(String query) {
        if (query == null) {
            return null;
        }

        // Remove markdown code blocks
        String cleaned = query.replaceAll("```sql\\s*", "");
        cleaned = cleaned.replaceAll("```\\s*", "");

        // Remove SQL: or SQLQuery: prefix
        cleaned = cleaned.replaceAll("(?i)^SQL(Query)?:\\s*", "");

        // Normalize whitespace
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.trim();

        // Remove surrounding quotes if present
        if ((cleaned.startsWith("\"") && cleaned.endsWith("\"")) ||
                (cleaned.startsWith("'") && cleaned.endsWith("'"))) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        log.debug("Cleaned SQL query: {}", cleaned);
        return cleaned.trim();
    }
}

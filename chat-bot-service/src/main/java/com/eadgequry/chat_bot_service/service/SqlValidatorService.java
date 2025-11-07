package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.exception.InvalidSqlException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Service to validate SQL queries and ensure only SELECT queries are allowed
 * Prevents dangerous operations like DROP, DELETE, UPDATE, INSERT, etc.
 */
@Service
@Slf4j
public class SqlValidatorService {

    @Value("${chatbot.strict-mode:true}")
    private boolean strictMode;

    private static final String[] FORBIDDEN_KEYWORDS = {
            "INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE",
            "CREATE", "REPLACE", "GRANT", "REVOKE", "EXEC", "EXECUTE",
            "CALL", "LOAD", "OUTFILE", "INFILE", "DUMPFILE"
    };

    private static final Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM_PATTERN = Pattern.compile("\\s+FROM\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * Validate SQL query - only SELECT queries are allowed
     */
    public void validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new InvalidSqlException("SQL query cannot be empty");
        }

        String trimmedQuery = query.trim();

        // Check if query starts with SELECT
        if (!SELECT_PATTERN.matcher(trimmedQuery).find()) {
            throw new InvalidSqlException(
                    "Only SELECT queries are allowed. Query must start with SELECT.",
                    "NOT_SELECT"
            );
        }

        // Check if query contains FROM clause
        if (!FROM_PATTERN.matcher(trimmedQuery).find()) {
            throw new InvalidSqlException(
                    "Invalid SELECT query: FROM clause is required",
                    "MISSING_FROM"
            );
        }

        // In strict mode, check for forbidden keywords
        if (strictMode) {
            checkForbiddenKeywords(trimmedQuery);
        }

        // Validate query structure (quotes, etc.)
        validateQueryStructure(trimmedQuery);

        log.debug("SQL query validated successfully: {}", query);
    }

    /**
     * Check for forbidden SQL keywords that could be dangerous
     */
    private void checkForbiddenKeywords(String query) {
        String upperQuery = query.toUpperCase();

        for (String forbiddenKeyword : FORBIDDEN_KEYWORDS) {
            // Check if keyword exists as a whole word (not part of another word)
            String regex = "\\b" + Pattern.quote(forbiddenKeyword) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(query).find()) {
                throw new InvalidSqlException(
                        String.format("Forbidden SQL keyword detected: %s. Only SELECT queries are allowed.", forbiddenKeyword),
                        "FORBIDDEN_KEYWORD"
                );
            }
        }
    }

    /**
     * Validate query structure (matching quotes, valid syntax)
     */
    private void validateQueryStructure(String query) {
        // Count single quotes
        long singleQuotes = query.chars().filter(ch -> ch == '\'').count();
        if (singleQuotes % 2 != 0) {
            throw new InvalidSqlException(
                    "Invalid SQL syntax: Unmatched single quote detected",
                    "UNMATCHED_QUOTE"
            );
        }

        // Count double quotes
        long doubleQuotes = query.chars().filter(ch -> ch == '"').count();
        if (doubleQuotes % 2 != 0) {
            throw new InvalidSqlException(
                    "Invalid SQL syntax: Unmatched double quote detected",
                    "UNMATCHED_QUOTE"
            );
        }

        // Count parentheses
        long openParens = query.chars().filter(ch -> ch == '(').count();
        long closeParens = query.chars().filter(ch -> ch == ')').count();
        if (openParens != closeParens) {
            throw new InvalidSqlException(
                    "Invalid SQL syntax: Unmatched parentheses detected",
                    "UNMATCHED_PARENTHESES"
            );
        }
    }

    /**
     * Clean SQL query from response (remove markdown, extra quotes, etc.)
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

        return cleaned.trim();
    }
}

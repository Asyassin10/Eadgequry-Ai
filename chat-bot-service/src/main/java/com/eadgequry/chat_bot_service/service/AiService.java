package com.eadgequry.chat_bot_service.service;

import com.eadgequry.chat_bot_service.config.AiApiProperties;
import com.eadgequry.chat_bot_service.dto.DatabaseSchemaDTO;
import com.eadgequry.chat_bot_service.exception.ChatBotException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * AI Service for generating SQL queries and natural language responses
 * Uses OpenRouter API (or compatible APIs like Groq)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    private final AiApiProperties aiApiProperties;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Generate SQL query from natural language question
     */
    public String generateSqlQuery(String question, DatabaseSchemaDTO schema,
                                   boolean isComparison, boolean isDetailsRequest,
                                   String previousError) {

        String prompt = buildQueryPrompt(question, schema, isComparison, isDetailsRequest, previousError);

        try {
            String response = callAiApi(prompt, aiApiProperties.getTemperatureQuery());
            log.debug("AI generated SQL query: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Failed to generate SQL query", e);
            throw new ChatBotException("Failed to generate SQL query: " + e.getMessage(), e);
        }
    }

    /**
     * Generate natural language answer from SQL results
     */
    public String generateAnswer(String question, String sqlQuery,
                                 List<Map<String, Object>> result,
                                 boolean isComparison, boolean isDetailsRequest) {

        String prompt = buildAnswerPrompt(question, sqlQuery, result, isComparison, isDetailsRequest);

        try {
            String response = callAiApi(prompt, aiApiProperties.getTemperatureAnswer());
            log.debug("AI generated answer: {}", response);
            return cleanAnswer(response);
        } catch (Exception e) {
            log.error("Failed to generate answer", e);
            throw new ChatBotException("Failed to generate answer: " + e.getMessage(), e);
        }
    }

    /**
     * Generate streaming answer from SQL results
     */
    public Flux<String> generateStreamingAnswer(String question, String sqlQuery,
                                                 List<Map<String, Object>> result,
                                                 boolean isComparison, boolean isDetailsRequest) {

        String prompt = buildAnswerPrompt(question, sqlQuery, result, isComparison, isDetailsRequest);

        return callAiApiStreaming(prompt, aiApiProperties.getTemperatureAnswer())
                .map(this::cleanAnswer)
                .onErrorResume(e -> {
                    log.error("Failed to generate streaming answer", e);
                    return Flux.just("Error: " + e.getMessage());
                });
    }

    /**
     * Build prompt for SQL query generation
     */
    private String buildQueryPrompt(String question, DatabaseSchemaDTO schema,
                                    boolean isComparison, boolean isDetailsRequest,
                                    String previousError) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a MySQL SELECT query to answer this question.\n\n");

        prompt.append("CRITICAL RULES:\n");
        prompt.append("1. Generate ONLY the SQL query, nothing else\n");
        prompt.append("2. Query MUST be syntactically correct\n");
        prompt.append("3. ALL quotes must be properly closed\n");
        prompt.append("4. Use LIKE with correct quotes: LIKE '%keyword%'\n");
        prompt.append("5. NO explanations, just the SQL query\n");
        prompt.append("6. NEVER return 'id' columns in SELECT\n\n");

        if (previousError != null) {
            prompt.append("PREVIOUS ERROR: ").append(previousError).append("\n");
            prompt.append("Correct this error in the new query.\n\n");
        }

        // Add schema information
        prompt.append("DATABASE SCHEMA:\n");
        prompt.append(formatSchemaInfo(schema));
        prompt.append("\n\n");

        // Special handling for comparisons
        if (isComparison) {
            prompt.append("🔍 COMPARISON REQUEST:\n");
            prompt.append("- Return: name, description, category_name for each item\n");
            prompt.append("- Return all attributes with positive values\n");
            prompt.append("- Maximum 2 items compared\n\n");
        }

        // Special handling for details requests
        if (isDetailsRequest) {
            prompt.append("📋 DETAILS REQUEST:\n");
            prompt.append("- Return ALL information: name, description, category\n");
            prompt.append("- Return ALL attributes with positive values\n");
            prompt.append("- User wants complete details\n\n");
        }

        prompt.append("Question: \"").append(question).append("\"\n");
        prompt.append("SQL:");

        return prompt.toString();
    }

    /**
     * Build prompt for answer generation
     */
    private String buildAnswerPrompt(String question, String sqlQuery,
                                     List<Map<String, Object>> result,
                                     boolean isComparison, boolean isDetailsRequest) {

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an AI assistant for a database query system.\n\n");

        prompt.append("Question: \"").append(question).append("\"\n");
        prompt.append("SQL Query: \"").append(sqlQuery).append("\"\n");
        prompt.append("Result: ").append(objectMapper.valueToTree(result).toString()).append("\n\n");

        if (result == null || result.isEmpty()) {
            prompt.append("⚠️ IMPORTANT: Result is empty, no data found.\n\n");
            prompt.append("- Explain clearly that no data matches the criteria\n");
            prompt.append("- DO NOT invent information\n");
            prompt.append("- Suggest alternatives\n\n");
        }

        prompt.append("INSTRUCTIONS:\n");
        prompt.append("- Respond in clear, professional language\n");
        prompt.append("- ⚠️ NEVER mention or display IDs\n");
        prompt.append("- Be concise and well-structured\n\n");

        if (isComparison) {
            prompt.append("🔍 DETAILED COMPARISON:\n");
            prompt.append("- Structure the comparison clearly\n");
            prompt.append("- Show: Name, Description, Category for each item\n");
            prompt.append("- List ALL criteria with ✅ emoji\n");
            prompt.append("- Format: ✅ Criterion name (don't show value if just 'yes')\n");
            prompt.append("- If numeric/text value: ✅ Criterion: value\n");
            prompt.append("- Compare important differences\n");
            prompt.append("- **CRITICAL**: Present as a comparison TABLE with columns\n\n");
        } else if (isDetailsRequest) {
            prompt.append("📋 COMPLETE DETAILS:\n");
            prompt.append("- Show: Name, Description, Category\n");
            prompt.append("- List ALL available criteria with ✅\n");
            prompt.append("- Format: ✅ Criterion (if value 'yes')\n");
            prompt.append("- Format: ✅ Criterion: value (if specific value)\n");
            prompt.append("- Organize by theme if possible\n");
            prompt.append("- Present in a structured TABLE format\n\n");
        } else {
            prompt.append("- Present results in TABLE format when possible\n");
            prompt.append("- If empty result, suggest alternatives\n");
            prompt.append("- If counting, show number clearly\n\n");
        }

        prompt.append("Provide a natural response:\n");
        prompt.append("Answer:");

        return prompt.toString();
    }

    /**
     * Format schema information for prompt
     */
    private String formatSchemaInfo(DatabaseSchemaDTO schema) {
        if (schema == null || schema.getTables() == null) {
            return "Schema information not available";
        }

        StringBuilder info = new StringBuilder();
        info.append("Database: ").append(schema.getDatabaseName()).append("\n");
        info.append("Type: ").append(schema.getDatabaseType()).append("\n\n");

        for (DatabaseSchemaDTO.TableInfo table : schema.getTables()) {
            info.append("Table '").append(table.getName()).append("':\n");

            if (table.getColumns() != null) {
                info.append("  Columns: ");
                info.append(table.getColumns().stream()
                        .map(col -> col.getName() + " (" + col.getType() + ")")
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("none"));
                info.append("\n");
            }

            if (table.getPrimaryKeys() != null && !table.getPrimaryKeys().isEmpty()) {
                info.append("  Primary Keys: ").append(String.join(", ", table.getPrimaryKeys())).append("\n");
            }

            if (table.getForeignKeys() != null && !table.getForeignKeys().isEmpty()) {
                info.append("  Foreign Keys: ");
                for (DatabaseSchemaDTO.ForeignKeyInfo fk : table.getForeignKeys()) {
                    info.append(fk.getColumn()).append(" -> ")
                        .append(fk.getReferencedTable()).append(".")
                        .append(fk.getReferencedColumn()).append("; ");
                }
                info.append("\n");
            }

            info.append("\n");
        }

        return info.toString();
    }

    /**
     * Call AI API (non-streaming)
     */
    private String callAiApi(String prompt, Double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", aiApiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful SQL and data analysis assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", temperature,
                "max_tokens", aiApiProperties.getMaxTokens()
        );

        try {
            String response = webClient.post()
                    .uri(aiApiProperties.getUrl())
                    .header("Authorization", "Bearer " + aiApiProperties.getKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(aiApiProperties.getTimeout()))
                    .block();

            return extractContent(response);
        } catch (Exception e) {
            log.error("AI API call failed", e);
            throw new ChatBotException("AI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Call AI API with streaming
     */
    private Flux<String> callAiApiStreaming(String prompt, Double temperature) {
        Map<String, Object> requestBody = Map.of(
                "model", aiApiProperties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful SQL and data analysis assistant."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", temperature,
                "max_tokens", aiApiProperties.getMaxTokens(),
                "stream", true
        );

        return webClient.post()
                .uri(aiApiProperties.getUrl())
                .header("Authorization", "Bearer " + aiApiProperties.getKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(aiApiProperties.getTimeout()))
                .map(this::extractStreamingContent)
                .filter(content -> content != null && !content.isEmpty());
    }

    /**
     * Extract content from AI API response
     */
    private String extractContent(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            throw new ChatBotException("Failed to parse AI response", e);
        }
    }

    /**
     * Extract content from streaming response
     */
    private String extractStreamingContent(String chunk) {
        try {
            if (chunk.startsWith("data: ")) {
                chunk = chunk.substring(6).trim();
            }
            if (chunk.equals("[DONE]")) {
                return "";
            }
            JsonNode root = objectMapper.readTree(chunk);
            JsonNode delta = root.path("choices").get(0).path("delta");
            return delta.path("content").asText("");
        } catch (Exception e) {
            log.warn("Failed to parse streaming chunk: {}", chunk, e);
            return "";
        }
    }

    /**
     * Clean answer (remove extra quotes, trim)
     */
    private String cleanAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        return answer.trim().replaceAll("^\"|\"$", "").replaceAll("^'|'$", "");
    }
}

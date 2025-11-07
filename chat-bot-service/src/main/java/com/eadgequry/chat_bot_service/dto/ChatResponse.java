package com.eadgequry.chat_bot_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private boolean success;
    private String question;
    private String sqlQuery;
    private List<Map<String, Object>> sqlResult;
    private String answer;
    private String error;

    public static ChatResponse success(String question, String sqlQuery, List<Map<String, Object>> sqlResult, String answer) {
        return ChatResponse.builder()
                .success(true)
                .question(question)
                .sqlQuery(sqlQuery)
                .sqlResult(sqlResult)
                .answer(answer)
                .build();
    }

    public static ChatResponse error(String error) {
        return ChatResponse.builder()
                .success(false)
                .error(error)
                .build();
    }

    public static ChatResponse greetingResponse(String question, String answer) {
        return ChatResponse.builder()
                .success(true)
                .question(question)
                .answer(answer)
                .build();
    }
}

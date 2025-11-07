package com.eadgequry.chat_bot_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 500, message = "Question must not exceed 500 characters")
    private String question;

    @NotNull(message = "Database config ID is required")
    private Long databaseConfigId;

    @NotNull(message = "User ID is required")
    private Long userId;
}

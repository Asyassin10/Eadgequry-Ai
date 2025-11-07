package com.eadgequry.chat_bot_service.controller;

import com.eadgequry.chat_bot_service.dto.ChatRequest;
import com.eadgequry.chat_bot_service.dto.ChatResponse;
import com.eadgequry.chat_bot_service.model.Conversation;
import com.eadgequry.chat_bot_service.service.ChatbotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "Natural language database query chatbot API")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatbotController {

    private static final Logger log = LoggerFactory.getLogger(ChatbotController.class);

    private final ChatbotService chatbotService;

    /**
     * Ask a question (non-streaming response)
     */
    @PostMapping("/ask")
    @Operation(summary = "Ask a question", description = "Ask a natural language question and get SQL query + answer")
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
        log.info("Received question: {} from user: {}", request.getQuestion(), request.getUserId());

        ChatResponse response = chatbotService.ask(request);

        log.info("Response generated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Ask a question with streaming response (Server-Sent Events)
     */
    @PostMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Ask a question with streaming", description = "Ask a question and get streaming answer (Server-Sent Events)")
    public Flux<String> askStreaming(@Valid @RequestBody ChatRequest request) {
        log.info("Received streaming question: {} from user: {}", request.getQuestion(), request.getUserId());

        return chatbotService.askStreaming(request)
                .delayElements(Duration.ofMillis(10)) // Small delay for better streaming experience
                .doOnComplete(() -> log.info("Streaming response completed"))
                .doOnError(error -> log.error("Streaming error", error));
    }

    /**
     * Get conversation history for a user
     */
    @GetMapping("/history/user/{userId}")
    @Operation(summary = "Get conversation history", description = "Get conversation history for a specific user")
    public ResponseEntity<List<Conversation>> getUserHistory(
            @Parameter(description = "User ID") @PathVariable Long userId) {

        List<Conversation> history = chatbotService.getConversationHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get conversation history for a session
     */
    @GetMapping("/history/session/{sessionId}")
    @Operation(summary = "Get session history", description = "Get conversation history for a specific session")
    public ResponseEntity<List<Conversation>> getSessionHistory(
            @Parameter(description = "Session ID") @PathVariable String sessionId) {

        List<Conversation> history = chatbotService.getSessionHistory(sessionId);
        return ResponseEntity.ok(history);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the chatbot service is running")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}

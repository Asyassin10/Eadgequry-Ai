package com.eadgequry.api_gatway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-2) // High priority to handle errors before default handler
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String message;
        String error;

        if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            error = "Unauthorized";
            message = "Authentication failed. Please provide a valid JWT token in the Authorization header.";
        } else if (ex instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
            error = "Forbidden";
            message = "You don't have permission to access this resource.";
        } else if (ex instanceof ResponseStatusException responseEx) {
            status = (HttpStatus) responseEx.getStatusCode();
            error = status.getReasonPhrase();
            message = responseEx.getReason() != null ? responseEx.getReason() : status.getReasonPhrase();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            error = "Internal Server Error";
            message = "An unexpected error occurred. Please try again later.";
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", error);
        errorAttributes.put("message", message);
        errorAttributes.put("path", exchange.getRequest().getPath().value());

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(errorAttributes);
        } catch (JsonProcessingException e) {
            bytes = "{\"error\":\"Error creating error response\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}

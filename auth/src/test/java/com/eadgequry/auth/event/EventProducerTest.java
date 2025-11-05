package com.eadgequry.auth.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private EventProducer eventProducer;

    @BeforeEach
    void setUp() {
        // Set topic names using ReflectionTestUtils
        ReflectionTestUtils.setField(eventProducer, "userRegisteredTopic", "user-registered");
        ReflectionTestUtils.setField(eventProducer, "passwordForgotTopic", "password-forgot");
        ReflectionTestUtils.setField(eventProducer, "emailUpdatedTopic", "email-updated");
    }

    @Test
    void publishUserRegistered_Success() {
        // Given
        UserRegisteredEvent event = new UserRegisteredEvent(
                1L,
                "John Doe",
                "john@example.com",
                "verification-token-123"
        );

        // When
        eventProducer.publishUserRegistered(event);

        // Then
        verify(kafkaTemplate).send(
                eq("user-registered"),
                eq("1"),
                eq(event)
        );
    }

    @Test
    void publishForgotPassword_Success() {
        // Given
        ForgotPasswordEvent event = new ForgotPasswordEvent(
                2L,
                "Jane Smith",
                "jane@example.com",
                "reset-token-456"
        );

        // When
        eventProducer.publishForgotPassword(event);

        // Then
        verify(kafkaTemplate).send(
                eq("password-forgot"),
                eq("2"),
                eq(event)
        );
    }

    @Test
    void publishEmailUpdated_Success() {
        // Given
        EmailUpdatedEvent event = new EmailUpdatedEvent(
                3L,
                "Bob Johnson",
                "old@example.com",
                "new@example.com",
                "verification-token-789"
        );

        // When
        eventProducer.publishEmailUpdated(event);

        // Then
        verify(kafkaTemplate).send(
                eq("email-updated"),
                eq("3"),
                eq(event)
        );
    }
}

package com.eadgequry.auth.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer service for publishing auth events
 */
@Service
public class EventProducer {

    private static final Logger logger = LoggerFactory.getLogger(EventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.user-registered}")
    private String userRegisteredTopic;

    @Value("${kafka.topics.password-forgot}")
    private String passwordForgotTopic;

    @Value("${kafka.topics.email-updated}")
    private String emailUpdatedTopic;

    public EventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publish user registered event
     */
    public void publishUserRegistered(UserRegisteredEvent event) {
        logger.info("Publishing UserRegisteredEvent for user: {}", event.email());
        kafkaTemplate.send(userRegisteredTopic, event.userId().toString(), event);
        logger.info("UserRegisteredEvent published successfully");
    }

    /**
     * Publish forgot password event
     */
    public void publishForgotPassword(ForgotPasswordEvent event) {
        logger.info("Publishing ForgotPasswordEvent for user: {}", event.email());
        kafkaTemplate.send(passwordForgotTopic, event.userId().toString(), event);
        logger.info("ForgotPasswordEvent published successfully");
    }

    /**
     * Publish email updated event
     */
    public void publishEmailUpdated(EmailUpdatedEvent event) {
        logger.info("Publishing EmailUpdatedEvent for user: {} -> {}", event.oldEmail(), event.newEmail());
        kafkaTemplate.send(emailUpdatedTopic, event.userId().toString(), event);
        logger.info("EmailUpdatedEvent published successfully");
    }
}

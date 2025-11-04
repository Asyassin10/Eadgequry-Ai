package com.eadgequry.notification.consumer;

import com.eadgequry.notification.dto.EmailUpdatedEvent;
import com.eadgequry.notification.dto.ForgotPasswordEvent;
import com.eadgequry.notification.dto.UserRegisteredEvent;
import com.eadgequry.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for authentication events
 * Listens to events from Auth Service and sends appropriate emails
 */
@Component
public class AuthEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final EmailService emailService;

    public AuthEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Listen for user registration events
     * Sends verification email to new users
     */
    @KafkaListener(topics = "${kafka.topics.user-registered}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleUserRegistered(UserRegisteredEvent event) {
        logger.info("Received UserRegisteredEvent for user: {}", event.email());

        try {
            emailService.sendVerificationEmail(event.email(), event.name(), event.verificationToken());
            logger.info("Successfully processed UserRegisteredEvent for user: {}", event.email());
        } catch (Exception e) {
            logger.error("Failed to process UserRegisteredEvent for user: {}", event.email(), e);
            // In production, you might want to implement retry logic or dead letter queue
        }
    }

    /**
     * Listen for forgot password events
     * Sends password reset email
     */
    @KafkaListener(topics = "${kafka.topics.password-forgot}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleForgotPassword(ForgotPasswordEvent event) {
        logger.info("Received ForgotPasswordEvent for user: {}", event.email());

        try {
            emailService.sendPasswordResetEmail(event.email(), event.name(), event.resetToken());
            logger.info("Successfully processed ForgotPasswordEvent for user: {}", event.email());
        } catch (Exception e) {
            logger.error("Failed to process ForgotPasswordEvent for user: {}", event.email(), e);
        }
    }

    /**
     * Listen for email update events
     * Sends notification to old email and verification to new email
     */
    @KafkaListener(topics = "${kafka.topics.email-updated}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleEmailUpdated(EmailUpdatedEvent event) {
        logger.info("Received EmailUpdatedEvent for user: {} -> {}", event.oldEmail(), event.newEmail());

        try {
            // Notify old email about the change
            emailService.sendEmailChangeNotificationOld(event.oldEmail(), event.name(), event.newEmail());

            // Send verification to new email
            emailService.sendEmailChangeVerificationNew(event.newEmail(), event.name(), event.verificationToken());

            logger.info("Successfully processed EmailUpdatedEvent for user: {}", event.newEmail());
        } catch (Exception e) {
            logger.error("Failed to process EmailUpdatedEvent for user: {}", event.newEmail(), e);
        }
    }
}

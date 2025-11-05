package com.eadgequry.notification.config;

import com.eadgequry.notification.dto.EmailUpdatedEvent;
import com.eadgequry.notification.dto.ForgotPasswordEvent;
import com.eadgequry.notification.dto.UserRegisteredEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka consumer configuration
 * Handles deserialization of Auth Service events to Notification DTOs
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Use ErrorHandlingDeserializer to handle deserialization errors gracefully
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);

        // Delegate to StringDeserializer for keys
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);

        // Delegate to JsonDeserializer for values
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JsonDeserializer configuration
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false); // Don't use type headers from producer

        // Map Auth Service event classes to Notification Service DTO classes
        config.put(JsonDeserializer.TYPE_MAPPINGS,
            "com.eadgequry.auth.event.UserRegisteredEvent:" + UserRegisteredEvent.class.getName() + "," +
            "com.eadgequry.auth.event.ForgotPasswordEvent:" + ForgotPasswordEvent.class.getName() + "," +
            "com.eadgequry.auth.event.EmailUpdatedEvent:" + EmailUpdatedEvent.class.getName()
        );

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}

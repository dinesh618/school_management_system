package com.school.management.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper; // For manual conversion if needed

    // Topics
    public static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";

    // Publish Notification Event
    public void publishNotificationEvent(String eventType, NotificationData notificationData) {
        // Wrap in type-safe generic event
        SchoolEvent<NotificationData> event = new SchoolEvent<>(eventType, notificationData, System.currentTimeMillis());
        publishEvent(NOTIFICATION_EVENTS_TOPIC, eventType, event);
    }

    // Internal publish method
    private void publishEvent(String topic, String eventType, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, eventType, event);

            future.whenComplete((result, exception) -> {
                if (exception == null) {
                    logger.info("Event published successfully: Topic={}, EventType={}, Offset={}",
                            topic, eventType, result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish event: Topic={}, EventType={}, Error={}",
                            topic, eventType, exception.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error publishing event to topic {}: {}", topic, e.getMessage(), e);
        }
    }

    // Generic Event Wrapper
    public static class SchoolEvent<T> {
        private String eventType;
        private T data;
        private long timestamp;

        public SchoolEvent() {}

        public SchoolEvent(String eventType, T data, long timestamp) {
            this.eventType = eventType;
            this.data = data;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public T getData() { return data; }
        public void setData(T data) { this.data = data; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    // Example NotificationData class
    public static class NotificationData {
        private Long userId;
        private String email;
        private String subject;
        private String message;
        private String type;
        private Object additionalData;

        public NotificationData() {}

        public NotificationData(Long userId, String email, String subject, String message, String type, Object additionalData) {
            this.userId = userId;
            this.email = email;
            this.subject = subject;
            this.message = message;
            this.type = type;
            this.additionalData = additionalData;
        }

        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public Object getAdditionalData() { return additionalData; }
        public void setAdditionalData(Object additionalData) { this.additionalData = additionalData; }
    }
}

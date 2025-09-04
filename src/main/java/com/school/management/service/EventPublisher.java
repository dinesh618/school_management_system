package com.school.management.service;


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

    // Topics
    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String COURSE_EVENTS_TOPIC = "course-events";
    public static final String ENROLLMENT_EVENTS_TOPIC = "enrollment-events";
    public static final String ASSIGNMENT_EVENTS_TOPIC = "assignment-events";
    public static final String SUBMISSION_EVENTS_TOPIC = "submission-events";
    public static final String ATTENDANCE_EVENTS_TOPIC = "attendance-events";
    public static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";

    public void publishUserEvent(String eventType, Object eventData) {
        publishEvent(USER_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishCourseEvent(String eventType, Object eventData) {
        publishEvent(COURSE_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishEnrollmentEvent(String eventType, Object eventData) {
        publishEvent(ENROLLMENT_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishAssignmentEvent(String eventType, Object eventData) {
        publishEvent(ASSIGNMENT_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishSubmissionEvent(String eventType, Object eventData) {
        publishEvent(SUBMISSION_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishAttendanceEvent(String eventType, Object eventData) {
        publishEvent(ATTENDANCE_EVENTS_TOPIC, eventType, eventData);
    }

    public void publishNotificationEvent(String eventType, Object eventData) {
        publishEvent(NOTIFICATION_EVENTS_TOPIC, eventType, eventData);
    }

    private void publishEvent(String topic, String eventType, Object eventData) {
        try {
            SchoolEvent event = new SchoolEvent(eventType, eventData, System.currentTimeMillis());

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

    // Event wrapper class
    public static class SchoolEvent {
        private String eventType;
        private Object data;
        private long timestamp;

        public SchoolEvent() {}

        public SchoolEvent(String eventType, Object data, long timestamp) {
            this.eventType = eventType;
            this.data = data;
            this.timestamp = timestamp;
        }

        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }

        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}

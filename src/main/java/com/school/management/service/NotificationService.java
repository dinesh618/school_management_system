package com.school.management.service;

import com.school.management.entity.Assignment;
import com.school.management.entity.Student;
import com.school.management.repository.EnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);



    @Autowired
    private EventPublisher eventPublisher;



    public void sendWelcomeMessage(Long userId, String email, String firstName) {
        try {
            EventPublisher.NotificationData notification = new EventPublisher.NotificationData(
                    userId,
                    email,
                    "Welcome to School Management System",
                    String.format("Welcome %s! Your account has been created successfully.", firstName),
                    "WELCOME_MESSAGE",
                    null
            );

            eventPublisher.publishNotificationEvent("SEND_EMAIL", notification);

        } catch (Exception e) {
            logger.error("Error sending welcome message: {}", e.getMessage(), e);
        }
    }

    private List<Student> getEnrolledStudents(Long courseId) {
        // Replace with actual repository query
        return List.of();
    }
}

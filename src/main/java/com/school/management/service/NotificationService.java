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
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private EventPublisher eventPublisher;

    public void sendAssignmentDueReminder(Assignment assignment) {
        try {
            // Get all students enrolled in the course
            List<Student> enrolledStudents = getEnrolledStudents(assignment.getCourse().getId());

            for (Student student : enrolledStudents) {
                NotificationData notification = new NotificationData(
                        student.getId(),
                        student.getEmail(),
                        "Assignment Due Reminder",
                        String.format("Assignment '%s' is due on %s",
                                assignment.getTitle(), assignment.getDueDate()),
                        "ASSIGNMENT_DUE_REMINDER",
                        assignment.getId()
                );

                // Publish notification event
                eventPublisher.publishNotificationEvent("SEND_EMAIL", notification);
            }

            logger.info("Sent assignment due reminders for assignment: {} to {} students",
                    assignment.getTitle(), enrolledStudents.size());

        } catch (Exception e) {
            logger.error("Error sending assignment due reminders: {}", e.getMessage(), e);
        }
    }

    public void sendOverdueAssignmentNotification(Assignment assignment) {
        try {
            // Notify the teacher about overdue assignment
            NotificationData notification = new NotificationData(
                    assignment.getCourse().getTeacher().getId(),
                    assignment.getCourse().getTeacher().getEmail(),
                    "Assignment Overdue",
                    String.format("Assignment '%s' in course '%s' is now overdue (Due: %s)",
                            assignment.getTitle(), assignment.getCourse().getCourseName(), assignment.getDueDate()),
                    "ASSIGNMENT_OVERDUE",
                    assignment.getId()
            );

            // Publish notification event
            eventPublisher.publishNotificationEvent("SEND_EMAIL", notification);

            logger.info("Sent overdue assignment notification for: {} to teacher: {}",
                    assignment.getTitle(), assignment.getCourse().getTeacher().getEmail());

        } catch (Exception e) {
            logger.error("Error sending overdue assignment notification: {}", e.getMessage(), e);
        }
    }

    public void sendEnrollmentConfirmation(Long studentId, String courseName) {
        try {
            // This would get student details and send confirmation
            NotificationData notification = new NotificationData(
                    studentId,
                    null, // Would fetch student email
                    "Enrollment Confirmation",
                    String.format("You have been successfully enrolled in %s", courseName),
                    "ENROLLMENT_CONFIRMATION",
                    null
            );

            eventPublisher.publishNotificationEvent("SEND_EMAIL", notification);

        } catch (Exception e) {
            logger.error("Error sending enrollment confirmation: {}", e.getMessage(), e);
        }
    }

    public void sendGradeNotification(Long studentId, String assignmentTitle, String grade) {
        try {
            NotificationData notification = new NotificationData(
                    studentId,
                    null, // Would fetch student email
                    "Assignment Graded",
                    String.format("Your assignment '%s' has been graded. Grade: %s",
                            assignmentTitle, grade),
                    "ASSIGNMENT_GRADED",
                    null
            );

            eventPublisher.publishNotificationEvent("SEND_EMAIL", notification);

        } catch (Exception e) {
            logger.error("Error sending grade notification: {}", e.getMessage(), e);
        }
    }

    public void sendWelcomeMessage(Long userId, String email, String firstName) {
        try {
            NotificationData notification = new NotificationData(
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
        // This would use a proper query to get enrolled students
        // For now, returning empty list
        return List.of();
    }

    public static class NotificationData {
        private Long userId;
        private String email;
        private String subject;
        private String message;
        private String type;
        private Long relatedEntityId;

        public NotificationData(Long userId, String email, String subject, String message,
                                String type, Long relatedEntityId) {
            this.userId = userId;
            this.email = email;
            this.subject = subject;
            this.message = message;
            this.type = type;
            this.relatedEntityId = relatedEntityId;
        }

        // Getters
        public Long getUserId() { return userId; }
        public String getEmail() { return email; }
        public String getSubject() { return subject; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public Long getRelatedEntityId() { return relatedEntityId; }
    }
}

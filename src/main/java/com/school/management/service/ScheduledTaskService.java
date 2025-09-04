package com.school.management.service;

import com.school.management.constant.Constant.*;
import com.school.management.entity.Assignment;
import com.school.management.entity.Attendance;
import com.school.management.entity.Student;
import com.school.management.repository.AssignmentRepository;
import com.school.management.repository.AttendanceRepository;
import com.school.management.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ScheduledTaskService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskService.class);

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private NotificationService notificationService;

    /**
     * Clear expired cache entries every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void clearExpiredCaches() {
        logger.info("Starting cache cleanup task");

        try {
            // Clear specific caches that might have expired data
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    // For frequently changing data, clear more often
                    if (cacheName.contains("submission") || cacheName.contains("attendance") ||
                            cacheName.contains("assignment")) {
                        cache.clear();
                        logger.debug("Cleared cache: {}", cacheName);
                    }
                }
            });

            logger.info("Cache cleanup task completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Check for assignments due in the next 24 hours and send reminders
     * Runs every 4 hours
     */
    @Scheduled(fixedRate = 14400000) // Every 4 hours
    public void checkUpcomingAssignments() {
        logger.info("Starting upcoming assignments check");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime tomorrow = now.plusDays(1);

            List<Assignment> upcomingAssignments = assignmentRepository
                    .findUpcomingAssignments(now, tomorrow);

            for (Assignment assignment : upcomingAssignments) {
                // Publish event for assignment due soon
                eventPublisher.publishAssignmentEvent("ASSIGNMENT_DUE_SOON", assignment);

                // Send notifications to enrolled students
                notificationService.sendAssignmentDueReminder(assignment);

                logger.info("Processed due reminder for assignment: {} (Due: {})",
                        assignment.getTitle(), assignment.getDueDate());
            }

            logger.info("Upcoming assignments check completed. Processed {} assignments",
                    upcomingAssignments.size());
        } catch (Exception e) {
            logger.error("Error checking upcoming assignments: {}", e.getMessage(), e);
        }
    }

    /**
     * Check for overdue assignments and mark them
     * Runs every 2 hours
     */
    @Scheduled(fixedRate = 7200000) // Every 2 hours
    public void checkOverdueAssignments() {
        logger.info("Starting overdue assignments check");

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Assignment> overdueAssignments = assignmentRepository.findOverdueAssignments(now);

            for (Assignment assignment : overdueAssignments) {
                // Publish event for overdue assignment
                eventPublisher.publishAssignmentEvent("ASSIGNMENT_OVERDUE", assignment);

                // Notify teacher about overdue assignment
                notificationService.sendOverdueAssignmentNotification(assignment);

                logger.info("Processed overdue assignment: {} (Due: {})",
                        assignment.getTitle(), assignment.getDueDate());
            }

            logger.info("Overdue assignments check completed. Processed {} assignments",
                    overdueAssignments.size());
        } catch (Exception e) {
            logger.error("Error checking overdue assignments: {}", e.getMessage(), e);
        }
    }

    /**
     * Calculate daily attendance statistics
     * Runs at 11:30 PM every day
     */
    @Scheduled(cron = "0 30 23 * * *") // 11:30 PM daily
    public void calculateDailyAttendanceStats() {
        logger.info("Starting daily attendance statistics calculation");

        try {
            LocalDate today = LocalDate.now();
            List<Attendance> todayAttendance = attendanceRepository.findByDate(today);

            // Calculate attendance statistics
            long totalRecords = todayAttendance.size();
            long presentCount = todayAttendance.stream()
                    .mapToLong(a -> a.getStatus()== AttendanceStatus.PRESENT? 1 : 0)
                    .sum();
            long absentCount = todayAttendance.stream()
                    .mapToLong(a -> a.getStatus() == AttendanceStatus.ABSENT ? 1 : 0)
                    .sum();
            long lateCount = todayAttendance.stream()
                    .mapToLong(a -> a.getStatus() == AttendanceStatus.LATE ? 1 : 0)
                    .sum();

            double attendanceRate = totalRecords > 0 ? (double) presentCount / totalRecords * 100 : 0;

            // Create attendance statistics object
            AttendanceStats stats = new AttendanceStats(today, totalRecords, presentCount,
                    absentCount, lateCount, attendanceRate);

            // Publish attendance statistics event
            eventPublisher.publishAttendanceEvent("DAILY_ATTENDANCE_CALCULATED", stats);

            logger.info("Daily attendance statistics calculated - Date: {}, Total: {}, Present: {}, " +
                            "Absent: {}, Late: {}, Rate: {:.2f}%",
                    today, totalRecords, presentCount, absentCount, lateCount, attendanceRate);

        } catch (Exception e) {
            logger.error("Error calculating daily attendance statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Update student GPA calculations
     * Runs at 2:00 AM every day
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM daily
    public void updateStudentGPAs() {
        logger.info("Starting student GPA update task");

        try {
            List<Student> activeStudents = studentRepository.findActiveStudentsByYearLevel(null);
            int updatedCount = 0;

            for (Student student : activeStudents) {
                try {
                    // Calculate GPA based on graded submissions
                    Double newGpa = calculateStudentGPA(student.getId());

                    if (newGpa != null && !Objects.equals(student.getGpa(), newGpa)) {
                        student.setGpa(newGpa);
                        studentRepository.save(student);
                        updatedCount++;

                        // Publish GPA update event
                        eventPublisher.publishUserEvent("STUDENT_GPA_UPDATED",
                                new GPAUpdateEvent(student.getId(), newGpa));
                    }
                } catch (Exception e) {
                    logger.error("Error updating GPA for student {}: {}", student.getId(), e.getMessage());
                }
            }

            logger.info("Student GPA update completed. Updated {} out of {} students",
                    updatedCount, activeStudents.size());

        } catch (Exception e) {
            logger.error("Error updating student GPAs: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old data and perform maintenance
     * Runs at 3:00 AM every Sunday
     */
    @Scheduled(cron = "0 0 3 * * SUN") // 3:00 AM every Sunday
    public void performWeeklyMaintenance() {
        logger.info("Starting weekly maintenance task");

        try {
            // Clear all caches for fresh start
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });

            // Publish maintenance event
            eventPublisher.publishNotificationEvent("WEEKLY_MAINTENANCE_COMPLETED",
                    "Weekly maintenance completed at " + LocalDateTime.now());

            logger.info("Weekly maintenance task completed successfully");
        } catch (Exception e) {
            logger.error("Error during weekly maintenance: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate daily system health report
     * Runs at 6:00 AM every day
     */

    private Double calculateStudentGPA(Long studentId) {
        // This would implement the actual GPA calculation logic
        // based on the student's graded submissions across all courses
        // For now, returning null to indicate no update needed
        return null;
    }

    // Helper classes for events
    public static class AttendanceStats {
        private LocalDate date;
        private long totalRecords;
        private long presentCount;
        private long absentCount;
        private long lateCount;
        private double attendanceRate;

        public AttendanceStats(LocalDate date, long totalRecords, long presentCount,
                               long absentCount, long lateCount, double attendanceRate) {
            this.date = date;
            this.totalRecords = totalRecords;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
            this.attendanceRate = attendanceRate;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public long getTotalRecords() { return totalRecords; }
        public long getPresentCount() { return presentCount; }
        public long getAbsentCount() { return absentCount; }
        public long getLateCount() { return lateCount; }
        public double getAttendanceRate() { return attendanceRate; }
    }

    public static class GPAUpdateEvent {
        private Long studentId;
        private Double newGpa;

        public GPAUpdateEvent(Long studentId, Double newGpa) {
            this.studentId = studentId;
            this.newGpa = newGpa;
        }

        public Long getStudentId() { return studentId; }
        public Double getNewGpa() { return newGpa; }
    }

    public static class SystemHealthReport {
        private LocalDate date;
        private long activeStudentCount;
        private long activeCourseCount;
        private long pendingSubmissions;
        private long timestamp;

        public SystemHealthReport(LocalDate date, long activeStudentCount, long activeCourseCount,
                                  long pendingSubmissions, long timestamp) {
            this.date = date;
            this.activeStudentCount = activeStudentCount;
            this.activeCourseCount = activeCourseCount;
            this.pendingSubmissions = pendingSubmissions;
            this.timestamp = timestamp;
        }

        // Getters
        public LocalDate getDate() { return date; }
        public long getActiveStudentCount() { return activeStudentCount; }
        public long getActiveCourseCount() { return activeCourseCount; }
        public long getPendingSubmissions() { return pendingSubmissions; }
        public long getTimestamp() { return timestamp; }
    }
}

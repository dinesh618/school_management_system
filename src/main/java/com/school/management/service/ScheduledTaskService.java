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


}

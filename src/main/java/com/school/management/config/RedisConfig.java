//package com.school.management.config;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.cache.RedisCacheConfiguration;
//import org.springframework.data.redis.cache.RedisCacheManager;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.time.Duration;
//import java.util.HashMap;
//import java.util.Map;
//
//@Configuration
//@EnableCaching
//public class RedisConfig {
//
//    @Value("${spring.redis.host:localhost}")
//    private String redisHost;
//
//    @Value("${spring.redis.port:6379}")
//    private int redisPort;
//
//    @Value("${spring.redis.password:}")
//    private String redisPassword;
//
//    @Bean
//    public LettuceConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
//        config.setHostName(redisHost);
//        config.setPort(redisPort);
//        if (!redisPassword.isEmpty()) {
//            config.setPassword(redisPassword);
//        }
//        return new LettuceConnectionFactory(config);
//    }
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        // Use String serializer for keys
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        // Use JSON serializer for values
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//
//        template.afterPropertiesSet();
//        return template;
//    }
//
//    @Bean
//    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
//        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(60)) // Default TTL: 1 hour
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
//
//        // Custom cache configurations with different TTLs
//        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
//
//        // User data - cache for 2 hours
//        cacheConfigurations.put("students", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("teachers", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("student", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("teacher", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("active-students", defaultConfig.entryTtl(Duration.ofHours(1)));
//        cacheConfigurations.put("active-teachers", defaultConfig.entryTtl(Duration.ofHours(1)));
//
//        // Course data - cache for 4 hours (less frequent changes)
//        cacheConfigurations.put("courses", defaultConfig.entryTtl(Duration.ofHours(4)));
//        cacheConfigurations.put("course", defaultConfig.entryTtl(Duration.ofHours(4)));
//        cacheConfigurations.put("active-courses", defaultConfig.entryTtl(Duration.ofHours(4)));
//        cacheConfigurations.put("courses-by-teacher", defaultConfig.entryTtl(Duration.ofHours(4)));
//        cacheConfigurations.put("courses-by-student", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("courses-by-semester-year", defaultConfig.entryTtl(Duration.ofHours(6)));
//
//        // Assignment data - cache for 1 hour (more dynamic)
//        cacheConfigurations.put("assignments", defaultConfig.entryTtl(Duration.ofHours(1)));
//        cacheConfigurations.put("assignment", defaultConfig.entryTtl(Duration.ofHours(1)));
//        cacheConfigurations.put("assignments-by-course", defaultConfig.entryTtl(Duration.ofHours(1)));
//        cacheConfigurations.put("assignments-by-teacher", defaultConfig.entryTtl(Duration.ofHours(1)));
//        cacheConfigurations.put("upcoming-assignments", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigurations.put("overdue-assignments", defaultConfig.entryTtl(Duration.ofMinutes(15)));
//
//        // Enrollment data - cache for 3 hours
//        cacheConfigurations.put("enrollments", defaultConfig.entryTtl(Duration.ofHours(3)));
//        cacheConfigurations.put("enrollment", defaultConfig.entryTtl(Duration.ofHours(3)));
//        cacheConfigurations.put("enrollments-by-student", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("enrollments-by-course", defaultConfig.entryTtl(Duration.ofHours(3)));
//
//        // Submission data - cache for 30 minutes (frequently updated)
//        cacheConfigurations.put("submissions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigurations.put("submission", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigurations.put("submissions-by-student", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigurations.put("submissions-by-assignment", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//        cacheConfigurations.put("ungraded-submissions", defaultConfig.entryTtl(Duration.ofMinutes(15)));
//
//        // Attendance data - cache for 2 hours
//        cacheConfigurations.put("attendance", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("attendance-record", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("attendance-by-student", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("attendance-by-course", defaultConfig.entryTtl(Duration.ofHours(2)));
//        cacheConfigurations.put("attendance-percentage", defaultConfig.entryTtl(Duration.ofHours(4)));
//
//        // Statistics - cache for 6 hours
//        cacheConfigurations.put("average-gpa", defaultConfig.entryTtl(Duration.ofHours(6)));
//        cacheConfigurations.put("teacher-course-count", defaultConfig.entryTtl(Duration.ofHours(6)));
//        cacheConfigurations.put("course-enrollment-count", defaultConfig.entryTtl(Duration.ofHours(4)));
//        cacheConfigurations.put("pending-grades-count", defaultConfig.entryTtl(Duration.ofMinutes(30)));
//
//        return RedisCacheManager.builder(connectionFactory)
//                .cacheDefaults(defaultConfig)
//                .withInitialCacheConfigurations(cacheConfigurations)
//                .build();
//    }
//}

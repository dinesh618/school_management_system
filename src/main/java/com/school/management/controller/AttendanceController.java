package com.school.management.controller;
import com.school.management.entity.Attendance;
import com.school.management.repository.AttendanceRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Attendance>> getAllAttendance(Pageable pageable) {
        Page<Attendance> attendance = attendanceRepository.findAll(pageable);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @attendanceRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id)")
    @Cacheable(value = "attendance-record", key = "#id")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        Optional<Attendance> attendance = attendanceRepository.findById(id);
        return attendance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-student", key = "#studentId")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(@PathVariable Long studentId) {
        List<Attendance> attendance = attendanceRepository.findByStudentId(studentId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-course", key = "#courseId")
    public ResponseEntity<List<Attendance>> getAttendanceByCourse(@PathVariable Long courseId) {
        List<Attendance> attendance = attendanceRepository.findByCourseId(courseId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-date", key = "#date")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Attendance> attendance = attendanceRepository.findByDate(date);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/course/{courseId}/date-range")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-course-date-range", key = "#courseId + '-' + #startDate + '-' + #endDate")
    public ResponseEntity<List<Attendance>> getAttendanceByCourseAndDateRange(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Attendance> attendance = attendanceRepository.findAttendanceByCourseAndDateRange(courseId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/student/{studentId}/date-range")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-student-date-range", key = "#studentId + '-' + #startDate + '-' + #endDate")
    public ResponseEntity<List<Attendance>> getAttendanceByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Attendance> attendance = attendanceRepository.findAttendanceByStudentAndDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"attendance", "attendance-by-student", "attendance-by-course", "attendance-by-date", "attendance-by-course-date-range", "attendance-by-student-date-range"}, allEntries = true)
    public ResponseEntity<Attendance> markAttendance(@Valid @RequestBody Attendance attendance, Authentication authentication) {
        // Check if attendance record already exists for this student, course, and date
        if (attendanceRepository.existsByStudentIdAndCourseIdAndDate(
                attendance.getStudent().getId(), attendance.getCourse().getId(), attendance.getDate())) {
            return ResponseEntity.badRequest().build();
        }

        attendance.setMarkedBy(authentication.getName());
        attendance.setMarkedAt(LocalDateTime.now());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return ResponseEntity.ok(savedAttendance);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"attendance", "attendance-record", "attendance-by-student", "attendance-by-course", "attendance-by-date", "attendance-by-course-date-range", "attendance-by-student-date-range"}, allEntries = true)
    public ResponseEntity<Attendance> updateAttendance(@PathVariable Long id, @Valid @RequestBody Attendance attendanceDetails, Authentication authentication) {
        Optional<Attendance> optionalAttendance = attendanceRepository.findById(id);
        if (optionalAttendance.isPresent()) {
            Attendance attendance = optionalAttendance.get();
            attendance.setStatus(attendanceDetails.getStatus());
            attendance.setRemarks(attendanceDetails.getRemarks());
            attendance.setMarkedBy(authentication.getName());
            attendance.setMarkedAt(LocalDateTime.now());

            Attendance updatedAttendance = attendanceRepository.save(attendance);
            return ResponseEntity.ok(updatedAttendance);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @CacheEvict(value = {"attendance", "attendance-record", "attendance-by-student", "attendance-by-course", "attendance-by-date", "attendance-by-course-date-range", "attendance-by-student-date-range"}, allEntries = true)
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{studentId}/course/{courseId}/present-days")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "present-days-count", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Long> getPresentDaysCount(@PathVariable Long studentId, @PathVariable Long courseId) {
        Long presentDays = attendanceRepository.countPresentDays(studentId, courseId);
        return ResponseEntity.ok(presentDays);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/total-days")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "total-days-count", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Long> getTotalDaysCount(@PathVariable Long studentId, @PathVariable Long courseId) {
        Long totalDays = attendanceRepository.countTotalDays(studentId, courseId);
        return ResponseEntity.ok(totalDays);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-percentage", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Double> getAttendancePercentage(@PathVariable Long studentId, @PathVariable Long courseId) {
        Double percentage = attendanceRepository.getAttendancePercentage(studentId, courseId);
        return ResponseEntity.ok(percentage != null ? percentage : 0.0);
    }
}

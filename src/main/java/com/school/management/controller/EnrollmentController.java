package com.school.management.controller;



import com.school.management.entity.Enrollment;
import com.school.management.repository.EnrollmentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/enrollments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EnrollmentController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollments", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Enrollment>> getAllEnrollments(Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository.findAll(pageable);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @enrollmentRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id)")
    @Cacheable(value = "enrollment", key = "#id")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Long id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        return enrollment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollments-by-student", key = "#studentId")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollments-by-course", key = "#courseId")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable Long courseId) {
        List<Enrollment> enrollments = enrollmentRepository.findActiveEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/semester/{semester}/year/{academicYear}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollments-by-semester-year", key = "#semester + '-' + #academicYear")
    public ResponseEntity<List<Enrollment>> getEnrollmentsBySemesterAndYear(@PathVariable String semester, @PathVariable String academicYear) {
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySemesterAndYear(semester, academicYear);
        return ResponseEntity.ok(enrollments);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @CacheEvict(value = {"enrollments", "enrollments-by-student", "enrollments-by-course", "enrollments-by-semester-year"}, allEntries = true)
    public ResponseEntity<Enrollment> createEnrollment(@Valid @RequestBody Enrollment enrollment) {
        // Check if enrollment already exists
        if (enrollmentRepository.existsByStudentIdAndCourseId(enrollment.getStudent().getId(), enrollment.getCourse().getId())) {
            return ResponseEntity.badRequest().build();
        }

        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
        return ResponseEntity.ok(savedEnrollment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @CacheEvict(value = {"enrollments", "enrollment", "enrollments-by-student", "enrollments-by-course", "enrollments-by-semester-year"}, allEntries = true)
    public ResponseEntity<Enrollment> updateEnrollment(@PathVariable Long id, @Valid @RequestBody Enrollment enrollmentDetails) {
        Optional<Enrollment> optionalEnrollment = enrollmentRepository.findById(id);
        if (optionalEnrollment.isPresent()) {
            Enrollment enrollment = optionalEnrollment.get();
            enrollment.setGrade(enrollmentDetails.getGrade());
            enrollment.setStatus(enrollmentDetails.getStatus());

            Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
            return ResponseEntity.ok(updatedEnrollment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    @CacheEvict(value = {"enrollments", "enrollment", "enrollments-by-student", "enrollments-by-course", "enrollments-by-semester-year"}, allEntries = true)
    public ResponseEntity<?> deleteEnrollment(@PathVariable Long id) {
        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment e = enrollment.get();
            e.setStatus(Enrollment.EnrollmentStatus.DROPPED);
            enrollmentRepository.save(e);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/course/{courseId}/count")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollment-count-by-course", key = "#courseId")
    public ResponseEntity<Long> getEnrollmentCountByCourse(@PathVariable Long courseId) {
        Long count = enrollmentRepository.countActiveEnrollmentsByCourse(courseId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/{studentId}/count")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "enrollment-count-by-student", key = "#studentId")
    public ResponseEntity<Long> getEnrollmentCountByStudent(@PathVariable Long studentId) {
        Long count = enrollmentRepository.countActiveEnrollmentsByStudent(studentId);
        return ResponseEntity.ok(count);
    }
}

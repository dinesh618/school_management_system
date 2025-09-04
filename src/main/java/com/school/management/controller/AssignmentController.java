package com.school.management.controller;
import com.school.management.entity.Assignment;
import com.school.management.repository.AssignmentRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/assignments")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Assignment>> getAllAssignments(Pageable pageable) {
        Page<Assignment> assignments = assignmentRepository.findAll(pageable);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/{id}")
    //@Cacheable(value = "assignment", key = "#id")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable Long id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        return assignment.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    @Cacheable(value = "assignments-by-course", key = "#courseId")
    public ResponseEntity<List<Assignment>> getAssignmentsByCourse(@PathVariable Long courseId) {
        List<Assignment> assignments = assignmentRepository.findByCourseIdAndIsActiveTrue(courseId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER') and #teacherId == authentication.principal.id or hasRole('ADMIN')")
    @Cacheable(value = "assignments-by-teacher", key = "#teacherId")
    public ResponseEntity<List<Assignment>> getAssignmentsByTeacher(@PathVariable Long teacherId) {
        List<Assignment> assignments = assignmentRepository.findAssignmentsByTeacher(teacherId);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/type/{type}")
    @Cacheable(value = "assignments-by-type", key = "#type")
    public ResponseEntity<List<Assignment>> getAssignmentsByType(@PathVariable Assignment.AssignmentType type) {
        List<Assignment> assignments = assignmentRepository.findByType(type);
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "overdue-assignments")
    public ResponseEntity<List<Assignment>> getOverdueAssignments() {
        List<Assignment> assignments = assignmentRepository.findOverdueAssignments(LocalDateTime.now());
        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/upcoming")
    @Cacheable(value = "upcoming-assignments")
    public ResponseEntity<List<Assignment>> getUpcomingAssignments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekFromNow = now.plusWeeks(1);
        List<Assignment> assignments = assignmentRepository.findUpcomingAssignments(now, oneWeekFromNow);
        return ResponseEntity.ok(assignments);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Assignment> createAssignment(@Valid @RequestBody Assignment assignment) {
        Assignment savedAssignment = assignmentRepository.save(assignment);
        return ResponseEntity.ok(savedAssignment);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"assignments", "assignment", "assignments-by-course", "assignments-by-teacher", "assignments-by-type", "overdue-assignments", "upcoming-assignments"}, allEntries = true)
    public ResponseEntity<Assignment> updateAssignment(@PathVariable Long id, @Valid @RequestBody Assignment assignmentDetails) {
        Optional<Assignment> optionalAssignment = assignmentRepository.findById(id);
        if (optionalAssignment.isPresent()) {
            Assignment assignment = optionalAssignment.get();
            assignment.setTitle(assignmentDetails.getTitle());
            assignment.setDescription(assignmentDetails.getDescription());
            assignment.setDueDate(assignmentDetails.getDueDate());
            assignment.setMaxPoints(assignmentDetails.getMaxPoints());
            assignment.setType(assignmentDetails.getType());
            assignment.setIsActive(assignmentDetails.getIsActive());

            Assignment updatedAssignment = assignmentRepository.save(assignment);
            return ResponseEntity.ok(updatedAssignment);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"assignments", "assignment", "assignments-by-course", "assignments-by-teacher", "assignments-by-type", "overdue-assignments", "upcoming-assignments"}, allEntries = true)
    public ResponseEntity<?> deleteAssignment(@PathVariable Long id) {
        Optional<Assignment> assignment = assignmentRepository.findById(id);
        if (assignment.isPresent()) {
            // Soft delete - set isActive to false
            Assignment a = assignment.get();
            a.setIsActive(false);
            assignmentRepository.save(a);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{assignmentId}/submission-count")
    @PreAuthorize("hasRole('TEACHER')")
    @Cacheable(value = "assignment-submission-count", key = "#assignmentId")
    public ResponseEntity<Long> getSubmissionCount(@PathVariable Long assignmentId) {
        Long submissionCount = assignmentRepository.countSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(submissionCount);
    }
}

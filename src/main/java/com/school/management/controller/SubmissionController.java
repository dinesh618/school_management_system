package com.school.management.controller;

import com.school.management.entity.Submission;
import com.school.management.repository.SubmissionRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/submissions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SubmissionController {

    @Autowired
    private SubmissionRepository submissionRepository;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "submissions", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Submission>> getAllSubmissions(Pageable pageable) {
        Page<Submission> submissions = submissionRepository.findAll(pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @submissionRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id)")
    @Cacheable(value = "submission", key = "#id")
    public ResponseEntity<Submission> getSubmissionById(@PathVariable Long id) {
        Optional<Submission> submission = submissionRepository.findById(id);
        return submission.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "submissions-by-student", key = "#studentId")
    public ResponseEntity<List<Submission>> getSubmissionsByStudent(@PathVariable Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/assignment/{assignmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "submissions-by-assignment", key = "#assignmentId")
    public ResponseEntity<List<Submission>> getSubmissionsByAssignment(@PathVariable Long assignmentId) {
        List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "submissions-by-course", key = "#courseId")
    public ResponseEntity<List<Submission>> getSubmissionsByCourse(@PathVariable Long courseId) {
        List<Submission> submissions = submissionRepository.findSubmissionsByCourse(courseId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "submissions-by-student-course", key = "#studentId + '-' + #courseId")
    public ResponseEntity<List<Submission>> getSubmissionsByStudentAndCourse(@PathVariable Long studentId, @PathVariable Long courseId) {
        List<Submission> submissions = submissionRepository.findSubmissionsByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/ungraded")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "ungraded-submissions")
    public ResponseEntity<List<Submission>> getUnGradedSubmissions() {
        List<Submission> submissions = submissionRepository.findUnGradedSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/assignment/{assignmentId}/ungraded")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "ungraded-submissions-by-assignment", key = "#assignmentId")
    public ResponseEntity<List<Submission>> getUnGradedSubmissionsByAssignment(@PathVariable Long assignmentId) {
        List<Submission> submissions = submissionRepository.findUnGradedSubmissionsByAssignment(assignmentId);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/late")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "late-submissions")
    public ResponseEntity<List<Submission>> getLateSubmissions() {
        List<Submission> submissions = submissionRepository.findLateSubmissions();
        return ResponseEntity.ok(submissions);
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @CacheEvict(value = {"submissions", "submissions-by-student", "submissions-by-assignment", "submissions-by-course", "submissions-by-student-course"}, allEntries = true)
    public ResponseEntity<Submission> createSubmission(@Valid @RequestBody Submission submission) {
        // Check if submission already exists
        if (submissionRepository.existsByStudentIdAndAssignmentId(submission.getStudent().getId(), submission.getAssignment().getId())) {
            return ResponseEntity.badRequest().build();
        }

        Submission savedSubmission = submissionRepository.save(submission);
        return ResponseEntity.ok(savedSubmission);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') and @submissionRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id")
    @CacheEvict(value = {"submissions", "submission", "submissions-by-student", "submissions-by-assignment", "submissions-by-course", "submissions-by-student-course"}, allEntries = true)
    public ResponseEntity<Submission> updateSubmission(@PathVariable Long id, @Valid @RequestBody Submission submissionDetails) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(id);
        if (optionalSubmission.isPresent()) {
            Submission submission = optionalSubmission.get();
            // Students can only update content before grading
            if (submission.getGrade() == null) {
                submission.setContent(submissionDetails.getContent());
                submission.setSubmittedAt(LocalDateTime.now());

                Submission updatedSubmission = submissionRepository.save(submission);
                return ResponseEntity.ok(updatedSubmission);
            } else {
                return ResponseEntity.badRequest().build(); // Cannot update graded submission
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/grade")
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"submissions", "submission", "submissions-by-assignment", "submissions-by-course", "ungraded-submissions", "ungraded-submissions-by-assignment"}, allEntries = true)
    public ResponseEntity<Submission> gradeSubmission(@PathVariable Long id, @RequestBody String grade, @RequestParam(required = false) String feedback, Authentication authentication) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(id);
        if (optionalSubmission.isPresent()) {
            Submission submission = optionalSubmission.get();
            submission.setGrade(grade);
            submission.setFeedback(feedback);
            submission.setGradedAt(LocalDateTime.now());
            submission.setGradedBy(authentication.getName());

            Submission updatedSubmission = submissionRepository.save(submission);
            return ResponseEntity.ok(updatedSubmission);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') and @submissionRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id or hasRole('ADMIN')")
    @CacheEvict(value = {"submissions", "submission", "submissions-by-student", "submissions-by-assignment", "submissions-by-course", "submissions-by-student-course"}, allEntries = true)
    public ResponseEntity<?> deleteSubmission(@PathVariable Long id) {
        Optional<Submission> submission = submissionRepository.findById(id);
        if (submission.isPresent()) {
            // Only allow deletion if not graded yet
            if (submission.get().getGrade() == null) {
                submissionRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().build(); // Cannot delete graded submission
            }
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/teacher/{teacherId}/pending-grades/count")
    @PreAuthorize("hasRole('TEACHER') and #teacherId == authentication.principal.id or hasRole('ADMIN')")
    @Cacheable(value = "pending-grades-count", key = "#teacherId")
    public ResponseEntity<Long> getPendingGradesCount(@PathVariable Long teacherId) {
        Long count = submissionRepository.countPendingGradesByTeacher(teacherId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/assignment/{assignmentId}/average-grade")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "average-grade-by-assignment", key = "#assignmentId")
    public ResponseEntity<Double> getAverageGradeByAssignment(@PathVariable Long assignmentId) {
        Double averageGrade = submissionRepository.getAverageGradeByAssignment(assignmentId);
        return ResponseEntity.ok(averageGrade != null ? averageGrade : 0.0);
    }
}
package com.school.management.repository;


import com.school.management.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByStudentId(Long studentId);

    List<Submission> findByAssignmentId(Long assignmentId);

    Optional<Submission> findByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    Boolean existsByStudentIdAndAssignmentId(Long studentId, Long assignmentId);

    @Query("SELECT s FROM Submission s WHERE s.assignment.course.id = :courseId")
    List<Submission> findSubmissionsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT s FROM Submission s WHERE s.student.id = :studentId AND s.assignment.course.id = :courseId")
    List<Submission> findSubmissionsByStudentAndCourse(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT s FROM Submission s WHERE s.grade IS NULL")
    List<Submission> findUnGradedSubmissions();

    @Query("SELECT s FROM Submission s WHERE s.assignment.id = :assignmentId AND s.grade IS NULL")
    List<Submission> findUnGradedSubmissionsByAssignment(@Param("assignmentId") Long assignmentId);

    @Query("SELECT s FROM Submission s WHERE s.isLate = true")
    List<Submission> findLateSubmissions();

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.course.teacher.id = :teacherId AND s.grade IS NULL")
    Long countPendingGradesByTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT AVG(CAST(s.grade AS double)) FROM Submission s WHERE s.assignment.id = :assignmentId AND s.grade IS NOT NULL")
    Double getAverageGradeByAssignment(@Param("assignmentId") Long assignmentId);
}

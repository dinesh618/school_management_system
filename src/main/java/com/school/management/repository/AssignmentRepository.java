package com.school.management.repository;


import com.school.management.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByCourseId(Long courseId);

    List<Assignment> findByCourseIdAndIsActiveTrue(Long courseId);

    List<Assignment> findByType(Assignment.AssignmentType type);

    @Query("SELECT a FROM Assignment a WHERE a.course.id = :courseId AND a.dueDate BETWEEN :startDate AND :endDate")
    List<Assignment> findAssignmentsByCourseDueBetween(@Param("courseId") Long courseId,
                                                       @Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Assignment a WHERE a.dueDate < :currentDate AND a.isActive = true")
    List<Assignment> findOverdueAssignments(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT a FROM Assignment a WHERE a.dueDate BETWEEN :currentDate AND :futureDate AND a.isActive = true")
    List<Assignment> findUpcomingAssignments(@Param("currentDate") LocalDateTime currentDate,
                                             @Param("futureDate") LocalDateTime futureDate);

    @Query("SELECT a FROM Assignment a JOIN a.course c WHERE c.teacher.id = :teacherId AND a.isActive = true")
    List<Assignment> findAssignmentsByTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId")
    Long countSubmissionsByAssignment(@Param("assignmentId") Long assignmentId);
}

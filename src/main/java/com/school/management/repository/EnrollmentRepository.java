package com.school.management.repository;


import com.school.management.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByCourseId(Long courseId);

    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    Boolean existsByStudent_StudentIdAndCourse_id(String studentId, Long courseId);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveEnrollmentsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ENROLLED'")
    List<Enrollment> findActiveEnrollmentsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ENROLLED'")
    Long countActiveEnrollmentsByCourse(@Param("courseId") Long courseId);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'ENROLLED'")
    Long countActiveEnrollmentsByStudent(@Param("studentId") Long studentId);

    @Query("SELECT e FROM Enrollment e WHERE e.course.semester = :semester AND e.course.academicYear = :academicYear AND e.status = 'ENROLLED'")
    List<Enrollment> findEnrollmentsBySemesterAndYear(@Param("semester") String semester, @Param("academicYear") String academicYear);
}

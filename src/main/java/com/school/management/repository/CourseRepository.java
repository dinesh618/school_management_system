package com.school.management.repository;


import com.school.management.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByCourseCode(String courseCode);

    Boolean existsByCourseCode(String courseCode);

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findBySemester(String semester);

    List<Course> findByAcademicYear(String academicYear);

    List<Course> findByIsActiveTrue();

    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId AND c.isActive = true")
    List<Course> findActiveCoursesByTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT c FROM Course c WHERE c.semester = :semester AND c.academicYear = :academicYear AND c.isActive = true")
    List<Course> findCoursesBySemesterAndYear(@Param("semester") String semester, @Param("academicYear") String academicYear);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId AND e.status = 'ENROLLED'")
    Long countEnrolledStudents(@Param("courseId") Long courseId);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student.id = :studentId AND e.status = 'ENROLLED'")
    List<Course> findCoursesByStudentId(@Param("studentId") Long studentId);
}

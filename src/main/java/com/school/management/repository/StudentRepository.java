package com.school.management.repository;


import com.school.management.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Student findByStudentId(String studentId);

    Boolean existsByStudentId(String studentId);

    List<Student> findByYearLevel(Integer yearLevel);

    List<Student> findByMajor(String major);

    @Query("SELECT s FROM Student s WHERE s.yearLevel = :yearLevel AND s.isActive = true")
    List<Student> findActiveStudentsByYearLevel(@Param("yearLevel") Integer yearLevel);

    @Query("SELECT s FROM Student s WHERE s.major = :major AND s.isActive = true")
    List<Student> findActiveStudentsByMajor(@Param("major") String major);

    @Query("SELECT AVG(s.gpa) FROM Student s WHERE s.isActive = true")
    Double getAverageGPA();

    @Query("SELECT s FROM Student s JOIN s.enrollments e WHERE e.course.id = :courseId AND e.status = 'ENROLLED'")
    List<Student> findStudentsEnrolledInCourse(@Param("courseId") Long courseId);
}

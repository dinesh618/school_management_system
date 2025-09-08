package com.school.management.repository;


import com.school.management.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    Optional<Teacher> findByEmployeeId(String employeeId);

    Boolean existsByEmployeeId(String employeeId);

    List<Teacher> findByDepartment(String department);

    List<Teacher> findBySpecialization(String specialization);

    @Query("SELECT t FROM Teacher t WHERE t.department = :department AND t.isActive = true")
    List<Teacher> findActiveTeachersByDepartment(@Param("department") String department);

    @Query("SELECT t FROM Teacher t WHERE t.specialization = :specialization AND t.isActive = true")
    List<Teacher> findActiveTeachersBySpecialization(@Param("specialization") String specialization);

    @Query("SELECT COUNT(c) FROM Course c WHERE c.teacher.id = :teacherId AND c.isActive = true")
    Long countActiveCoursesByTeacher(@Param("teacherId") Long teacherId);

    @Query("SELECT t FROM Teacher t WHERE t.id = :id")
    Teacher findByTeacherId(@Param("id")Long id);


}

package com.school.management.repository;

import com.school.management.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
   @Query(value = "select  * from  department d where d.department_name = :departmentName",nativeQuery = true)
    Department findByDepartmentName(String departmentName);
    @Query(value = "select  * from  department d where d.id = :id",nativeQuery = true)
    Department findByDepartmentId(Long id);
}

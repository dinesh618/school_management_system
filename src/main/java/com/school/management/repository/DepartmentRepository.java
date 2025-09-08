package com.school.management.repository;

import com.school.management.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Department findByDepartmentName(String departmentName);

    Department findByDepartmentId(Long id);
}

package com.school.management.controller;
import com.school.management.entity.Teacher;
import com.school.management.repository.TeacherRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/teachers")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TeacherController {

    @Autowired
    private TeacherRepository teacherRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "teachers", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Teacher>> getAllTeachers(Pageable pageable) {
        Page<Teacher> teachers = teacherRepository.findAll(pageable);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Cacheable(value = "active-teachers")
    public ResponseEntity<List<Teacher>> getActiveTeachers() {
        List<Teacher> activeTeachers = teacherRepository.findActiveTeachersByDepartment(null);
        return ResponseEntity.ok(activeTeachers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    @Cacheable(value = "teacher", key = "#id")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        return teacher.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee-id/{employeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "teacher-by-employee-id", key = "#employeeId")
    public ResponseEntity<Teacher> getTeacherByEmployeeId(@PathVariable String employeeId) {
        Optional<Teacher> teacher = teacherRepository.findByEmployeeId(employeeId);
        return teacher.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Cacheable(value = "teachers-by-department", key = "#department")
    public ResponseEntity<List<Teacher>> getTeachersByDepartment(@PathVariable String department) {
        List<Teacher> teachers = teacherRepository.findActiveTeachersByDepartment(department);
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/specialization/{specialization}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    @Cacheable(value = "teachers-by-specialization", key = "#specialization")
    public ResponseEntity<List<Teacher>> getTeachersBySpecialization(@PathVariable String specialization) {
        List<Teacher> teachers = teacherRepository.findActiveTeachersBySpecialization(specialization);
        return ResponseEntity.ok(teachers);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and #id == authentication.principal.id)")
    @CacheEvict(value = {"teachers", "teacher", "active-teachers", "teachers-by-department", "teachers-by-specialization"}, allEntries = true)
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @Valid @RequestBody Teacher teacherDetails) {
        Optional<Teacher> optionalTeacher = teacherRepository.findById(id);
        if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();
            teacher.setFirstName(teacherDetails.getFirstName());
            teacher.setLastName(teacherDetails.getLastName());
            teacher.setEmail(teacherDetails.getEmail());
            teacher.setDepartment(teacherDetails.getDepartment());
            teacher.setSpecialization(teacherDetails.getSpecialization());
            teacher.setIsActive(teacherDetails.getIsActive());

            Teacher updatedTeacher = teacherRepository.save(teacher);
            return ResponseEntity.ok(updatedTeacher);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"teachers", "teacher", "active-teachers", "teachers-by-department", "teachers-by-specialization"}, allEntries = true)
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        if (teacher.isPresent()) {
            // Soft delete - set isActive to false
            Teacher t = teacher.get();
            t.setIsActive(false);
            teacherRepository.save(t);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{teacherId}/course-count")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('TEACHER') and #teacherId == authentication.principal.id)")
    @Cacheable(value = "teacher-course-count", key = "#teacherId")
    public ResponseEntity<Long> getActiveCourseCount(@PathVariable Long teacherId) {
        Long courseCount = teacherRepository.countActiveCoursesByTeacher(teacherId);
        return ResponseEntity.ok(courseCount);
    }
}
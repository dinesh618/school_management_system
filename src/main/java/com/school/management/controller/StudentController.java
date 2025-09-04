package com.school.management.controller;



import com.school.management.dto.StudentDto;
import com.school.management.entity.Student;
import com.school.management.repository.StudentRepository;
import com.school.management.service.StudentService;
import exception.CustomException;
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
@RequestMapping("/students")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StudentService studentService;;

    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Student>> getAllStudents(Pageable pageable) {
        Page<Student> students = studentRepository.findAll(pageable);
        return ResponseEntity.ok(students);
    }

//    @GetMapping("/active")
//    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
////    @Cacheable(value = "active-students")
//    public ResponseEntity<List<Student>> getActiveStudents() {
//        List<Student> activeStudents = studentRepository.findActiveStudentsByYearLevel(null);
//        return ResponseEntity.ok(activeStudents);
//    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and #id == authentication.principal.id)")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
         StudentDto dto = studentService.getStudentById(id);
        return ResponseEntity.ok(dto);
    }

//    @GetMapping("/student-id/{studentId}")
//    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
//    @Cacheable(value = "student-by-student-id", key = "#studentId")
//    public ResponseEntity<Student> getStudentByStudentId(@PathVariable String studentId) {
//        Optional<Student> student = studentRepository.findByStudentId(studentId);
//        return student.map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/year/{yearLevel}")
//    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
//    @Cacheable(value = "students-by-year", key = "#yearLevel")
//    public ResponseEntity<List<Student>> getStudentsByYearLevel(@PathVariable Integer yearLevel) {
//        List<Student> students = studentRepository.findActiveStudentsByYearLevel(yearLevel);
//        return ResponseEntity.ok(students);
//    }

    @GetMapping("/major/{major}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "students-by-major", key = "#major")
    public ResponseEntity<List<Student>> getStudentsByMajor(@PathVariable String major) {
        List<Student> students = studentRepository.findActiveStudentsByMajor(major);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<List<Student>> getStudentsEnrolledInCourse(@PathVariable Long courseId) {
        List<Student> students = studentRepository.findStudentsEnrolledInCourse(courseId);
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('STUDENT') and #id == authentication.principal.id)")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @Valid @RequestBody Student studentDetails) {
        Optional<Student> optionalStudent = studentRepository.findById(id);
        if (optionalStudent.isPresent()) {
            Student student = optionalStudent.get();
            student.setFirstName(studentDetails.getFirstName());
            student.setLastName(studentDetails.getLastName());
            student.setEmail(studentDetails.getEmail());
            student.setYearLevel(studentDetails.getYearLevel());
            student.setMajor(studentDetails.getMajor());
            student.setGpa(studentDetails.getGpa());
            student.setIsActive(studentDetails.getIsActive());

            Student updatedStudent = studentRepository.save(student);
            return ResponseEntity.ok(updatedStudent);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = {"students", "student", "active-students", "students-by-year", "students-by-major"}, allEntries = true)
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) {
            // Soft delete - set isActive to false
            Student s = student.get();
            s.setIsActive(false);
            studentRepository.save(s);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/statistics/average-gpa")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "average-gpa")
    public ResponseEntity<Double> getAverageGPA() {
        Double avgGpa = studentRepository.getAverageGPA();
        return ResponseEntity.ok(avgGpa != null ? avgGpa : 0.0);
    }
}

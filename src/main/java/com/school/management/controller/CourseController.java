package com.school.management.controller;


import com.school.management.constant.ResponseMessage;
import com.school.management.dto.CourseDto;
import com.school.management.entity.Course;
import com.school.management.entity.Teacher;
import com.school.management.repository.CourseRepository;
import com.school.management.repository.TeacherRepository;
import com.school.management.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private CourseService courseService;



    @GetMapping
    @Cacheable(value = "courses", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Course>> getAllCourses(Pageable pageable) {
        Page<Course> courses = courseRepository.findAll(pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Course>> getActiveCourses() {
        List<Course> activeCourses = courseRepository.findByIsActiveTrue();
        return ResponseEntity.ok(activeCourses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
      CourseDto  courseDto = courseService.getCourseById(id);
          return ResponseEntity.ok(courseDto);
    }

    @GetMapping("/code/{courseCode}")
    public ResponseEntity<Course> getCourseByCourseCode(@PathVariable String courseCode) {
        Optional<Course> course = courseRepository.findByCourseCode(courseCode);
        return course.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('STUDENT')")
    @Cacheable(value = "courses-by-teacher", key = "#teacherId")
    public ResponseEntity<List<Course>> getCoursesByTeacher(@PathVariable Long teacherId) {
        List<Course> courses = courseRepository.findActiveCoursesByTeacher(teacherId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "courses-by-student", key = "#studentId")
    public ResponseEntity<List<Course>> getCoursesByStudent(@PathVariable Long studentId) {
        List<Course> courses = courseRepository.findCoursesByStudentId(studentId);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/semester/{semester}/year/{academicYear}")
    @Cacheable(value = "courses-by-semester-year", key = "#semester + '-' + #academicYear")
    public ResponseEntity<List<Course>> getCoursesBySemesterAndYear(@PathVariable String semester, @PathVariable String academicYear) {
        List<Course> courses = courseRepository.findCoursesBySemesterAndYear(semester, academicYear);
        return ResponseEntity.ok(courses);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    public ResponseEntity<ResponseMessage> createCourse(@Valid @RequestBody CourseDto courseDto) {
        // Check if course code already exists
        if (courseRepository.existsByCourseCode(courseDto.getCourseCode())) {
            return ResponseEntity.badRequest().build();
        }

        Teacher teacher = teacherRepository.findByTeacherId(Long.valueOf(courseDto.getTeacherId()));
        Course course = new Course();
        course.setCourseCode(courseDto.getCourseCode());
        course.setCourseName(courseDto.getCourseName());
        course.setDescription(courseDto.getDescription());
        course.setRoom(courseDto.getRoom());
        course.setAcademicYear(courseDto.getAcademicYear());
        course.setCredits(courseDto.getCredits());
        course.setAcademicYear(courseDto.getAcademicYear());
        course.setSemester(courseDto.getSemester());
        course.setMaxStudents(courseDto.getMaxStudents());
        course.setSemester(courseDto.getSemester());
        course.setSchedule(courseDto.getSchedule());
        course.setTeacher(teacher);

        Course savedCourse = courseRepository.save(course);
        return  new ResponseEntity<>( new ResponseMessage("course saved successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @CacheEvict(value = {"courses", "course", "active-courses", "courses-by-teacher", "courses-by-semester-year", "course-by-code"}, allEntries = true)
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody Course courseDetails) {
        Optional<Course> optionalCourse = courseRepository.findById(id);
        if (optionalCourse.isPresent()) {
            Course course = optionalCourse.get();
            course.setCourseName(courseDetails.getCourseName());
            course.setDescription(courseDetails.getDescription());
            course.setCredits(courseDetails.getCredits());
            course.setSemester(courseDetails.getSemester());
            course.setAcademicYear(courseDetails.getAcademicYear());
            course.setSchedule(courseDetails.getSchedule());
            course.setRoom(courseDetails.getRoom());
            course.setMaxStudents(courseDetails.getMaxStudents());
            course.setIsActive(courseDetails.getIsActive());

            Course updatedCourse = courseRepository.save(course);
            return ResponseEntity.ok(updatedCourse);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @CacheEvict(value = {"courses", "course", "active-courses", "courses-by-teacher", "courses-by-semester-year", "course-by-code"}, allEntries = true)
    public ResponseEntity<?> deleteCourse(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isPresent()) {
            // Soft delete - set isActive to false
            Course c = course.get();
            c.setIsActive(false);
            
            courseRepository.save(c);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{courseId}/enrollment-count")
    @Cacheable(value = "course-enrollment-count", key = "#courseId")
    public ResponseEntity<Long> getEnrollmentCount(@PathVariable Long courseId) {
        Long enrollmentCount = courseRepository.countEnrolledStudents(courseId);
        return ResponseEntity.ok(enrollmentCount);
    }
}

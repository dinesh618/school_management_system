package com.school.management.controller;
import com.school.management.constant.Constant;
import com.school.management.dto.AttendenceDto;
import com.school.management.entity.Attendance;
import com.school.management.entity.Course;
import com.school.management.entity.Student;
import com.school.management.repository.AttendanceRepository;
import com.school.management.repository.CourseRepository;
import com.school.management.repository.StudentRepository;
import exception.CustomException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/attendance")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
     private CourseRepository courseRepository;
    @GetMapping
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public ResponseEntity<Page<Attendance>> getAllAttendance(Pageable pageable) {
        Page<Attendance> attendance = attendanceRepository.findAll(pageable);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or (hasRole('STUDENT') and @attendanceRepository.findById(#id).orElse(null)?.student?.id == authentication.principal.id)")
    public ResponseEntity<AttendenceDto> getAttendanceById(@PathVariable Long id) {
        Optional<Attendance> attendance = attendanceRepository.findById(id);
         if(attendance.isPresent())
         {
             AttendenceDto attendeeInformation = new AttendenceDto();
               attendeeInformation.setCourseId(attendance.get().getCourse().getId());
             attendeeInformation.setStudentId(attendance.get().getStudent().getStudentId());
             attendeeInformation.setStatus(attendance.get().getStatus());
             attendeeInformation.setMarkedBy(attendance.get().getMarkedBy());
             attendeeInformation.setMarkedAt(attendance.get().getMarkedAt());
               return  new ResponseEntity<>(attendeeInformation,HttpStatus.OK);


         }
         throw new CustomException("Error to fetch attende detail by Id");

    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-student", key = "#studentId")
    public ResponseEntity<List<Attendance>> getAttendanceByStudent(@PathVariable Long studentId) {
        List<Attendance> attendance = attendanceRepository.findByStudentId(studentId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-course", key = "#courseId")
    public ResponseEntity<List<Attendance>> getAttendanceByCourse(@PathVariable Long courseId) {
        List<Attendance> attendance = attendanceRepository.findByCourseId(courseId);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-date", key = "#date")
    public ResponseEntity<List<Attendance>> getAttendanceByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Attendance> attendance = attendanceRepository.findByDate(date);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/course/{courseId}/date-range")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-course-date-range", key = "#courseId + '-' + #startDate + '-' + #endDate")
    public ResponseEntity<List<Attendance>> getAttendanceByCourseAndDateRange(
            @PathVariable Long courseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Attendance> attendance = attendanceRepository.findAttendanceByCourseAndDateRange(courseId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/student/{studentId}/date-range")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-by-student-date-range", key = "#studentId + '-' + #startDate + '-' + #endDate")
    public ResponseEntity<List<Attendance>> getAttendanceByStudentAndDateRange(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Attendance> attendance = attendanceRepository.findAttendanceByStudentAndDateRange(studentId, startDate, endDate);
        return ResponseEntity.ok(attendance);
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> markAttendance(@RequestBody AttendenceDto attendenceDto,Authentication authentication) {
        // Check if attendance record already exists for this student, course, and date
          Student student = studentRepository.findByStudentId(attendenceDto.getStudentId());
          Course course = courseRepository.findByCourseId(attendenceDto.getCourseId());
        Attendance attendee  = attendanceRepository.findByMarkedAtAndMarkedBy(attendenceDto.getMarkedAt(),authentication.getName());


    if(Objects.isNull(attendee)){
        Attendance attendance = new Attendance();
        attendance.setMarkedBy(authentication.getName());
        attendance.setCourse(course);
        attendance.setStatus(Constant.AttendanceStatus.PRESENT);
        attendance.setStudent(student);
        attendance.setMarkedAt(LocalDateTime.now());
        attendance.setRemarks(attendenceDto.getRemarks());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return  new ResponseEntity<>("attendance marked successfully", HttpStatus.OK);
    }

    throw new CustomException("attendance already marked");

    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @CacheEvict(value = {"attendance", "attendance-record", "attendance-by-student", "attendance-by-course", "attendance-by-date", "attendance-by-course-date-range", "attendance-by-student-date-range"}, allEntries = true)
    public ResponseEntity<Attendance> updateAttendance(@PathVariable Long id, @Valid @RequestBody Attendance attendanceDetails, Authentication authentication) {
        Optional<Attendance> optionalAttendance = attendanceRepository.findById(id);
        if (optionalAttendance.isPresent()) {
            Attendance attendance = optionalAttendance.get();
            attendance.setStatus(attendanceDetails.getStatus());
            attendance.setRemarks(attendanceDetails.getRemarks());
            attendance.setMarkedBy(authentication.getName());
            attendance.setMarkedAt(LocalDateTime.now());

            Attendance updatedAttendance = attendanceRepository.save(attendance);
            return ResponseEntity.ok(updatedAttendance);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN')")
    @CacheEvict(value = {"attendance", "attendance-record", "attendance-by-student", "attendance-by-course", "attendance-by-date", "attendance-by-course-date-range", "attendance-by-student-date-range"}, allEntries = true)
    public ResponseEntity<?> deleteAttendance(@PathVariable Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/student/{studentId}/course/{courseId}/present-days")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "present-days-count", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Long> getPresentDaysCount(@PathVariable Long studentId, @PathVariable Long courseId) {
        Long presentDays = attendanceRepository.countPresentDays(studentId, courseId);
        return ResponseEntity.ok(presentDays);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/total-days")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "total-days-count", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Long> getTotalDaysCount(@PathVariable Long studentId, @PathVariable Long courseId) {
        Long totalDays = attendanceRepository.countTotalDays(studentId, courseId);
        return ResponseEntity.ok(totalDays);
    }

    @GetMapping("/student/{studentId}/course/{courseId}/percentage")
    @PreAuthorize("hasRole('STUDENT') and #studentId == authentication.principal.id or hasRole('TEACHER') or hasRole('ADMIN')")
    @Cacheable(value = "attendance-percentage", key = "#studentId + '-' + #courseId")
    public ResponseEntity<Double> getAttendancePercentage(@PathVariable Long studentId, @PathVariable Long courseId) {
        Double percentage = attendanceRepository.getAttendancePercentage(studentId, courseId);
        return ResponseEntity.ok(percentage != null ? percentage : 0.0);
    }
}

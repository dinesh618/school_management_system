package com.school.management.repository;


import com.school.management.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentId(Long studentId);



    List<Attendance> findByCourseId(Long courseId);

    List<Attendance> findByDate(LocalDate date);

    Optional<Attendance> findByStudentIdAndCourseIdAndDate(Long studentId, Long courseId, LocalDate date);

        @Query("SELECT a FROM Attendance a WHERE a.course.id = :courseId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findAttendanceByCourseAndDateRange(@Param("courseId") Long courseId,
                                                        @Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.student.id = :studentId AND a.date BETWEEN :startDate AND :endDate")
    List<Attendance> findAttendanceByStudentAndDateRange(@Param("studentId") Long studentId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.course.id = :courseId AND a.status = 'PRESENT'")
    Long countPresentDays(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.id = :studentId AND a.course.id = :courseId")
    Long countTotalDays(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT (COUNT(a) * 100.0 / (SELECT COUNT(a2) FROM Attendance a2 WHERE a2.student.id = :studentId AND a2.course.id = :courseId)) " +
            "FROM Attendance a WHERE a.student.id = :studentId AND a.course.id = :courseId AND a.status = 'PRESENT'")
    Double getAttendancePercentage(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query(value = "select * from attendance a where a.marked_at = :date and marked_by = :markedBy",nativeQuery = true)
    Attendance findByMarkedAtAndMarkedBy(@Param("date") LocalDateTime date, @Param("markedBy")String markedBy );
}

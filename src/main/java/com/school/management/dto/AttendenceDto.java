package com.school.management.dto;
import com.school.management.constant.Constant;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AttendenceDto {

    private  String studentId;

    private Long courseId;

    private LocalDate date;

    private Constant.AttendanceStatus status;

    private String remarks;

    private LocalDateTime markedAt;

    private String markedBy;

}

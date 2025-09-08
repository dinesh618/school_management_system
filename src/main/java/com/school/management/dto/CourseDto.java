package com.school.management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {

    private String courseCode;

    private String courseName;

    private String description;

    private Integer credits;

    private String semester;

    private String academicYear;

    private String schedule;

    private String room;

    private Integer maxStudents;

    private Boolean isActive = true;

    private Integer teacherId;


}

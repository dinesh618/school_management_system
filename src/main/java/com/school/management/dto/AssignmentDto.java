package com.school.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.school.management.entity.Assignment;
import com.school.management.entity.Course;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentDto {

    @NotBlank
    @Column(name = "title")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime dueDate;

    @Positive
    @Column(name = "max_points")
    private Integer maxPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type")
    private Assignment.AssignmentType type;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Long courseId;

}

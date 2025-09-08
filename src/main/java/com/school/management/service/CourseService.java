package com.school.management.service;

import com.school.management.dto.CourseDto;
import com.school.management.entity.Course;
import com.school.management.repository.CourseRepository;
import exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service

public class CourseService {

    @Autowired
    private CourseRepository courseRepository;



    @Cacheable(value = "course-by-code", key = "#p0")
    public CourseDto getCourseById(Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if(course.isPresent())
        {
            CourseDto courseDto = new CourseDto();
               courseDto.setCourseName(course.get().getCourseName());
               courseDto.setCourseCode(course.get().getCourseCode());
               courseDto.setDescription(course.get().getDescription());
               courseDto.setRoom(course.get().getRoom());
               courseDto.setMaxStudents(course.get().getMaxStudents());
               courseDto.setSemester(course.get().getSemester());
               courseDto.setIsActive(course.get().getIsActive());
               courseDto.setAcademicYear(course.get().getAcademicYear());
               courseDto.setSchedule(course.get().getSchedule());
             return courseDto;
        }
        else throw new CustomException("Unable to identify course of this Id");

    }
}

package com.school.management.service;

import com.school.management.dto.StudentDto;
import com.school.management.entity.Student;
import com.school.management.repository.StudentRepository;
import exception.CustomException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Cacheable(value = "student", key = "#id")
    public StudentDto getStudentById(Long id) {
        Optional<Student> student = studentRepository.findById(id);
        if(Objects.nonNull(student)) {
            StudentDto studentDto = new StudentDto();
            studentDto.setFirstName(student.get().getFirstName());
            studentDto.setLastName(student.get().getLastName());
            studentDto.setEmail(student.get().getEmail());
            studentDto.setPassword(student.get().getPassword());
            studentDto.setMajor(student.get().getMajor());
            studentDto.setYearLevel(student.get().getYearLevel());
            return studentDto;
        }
        else
            throw new CustomException("Student Id is not exist");

    }
}


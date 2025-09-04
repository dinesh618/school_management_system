package com.school.management.dto;

public class StudentDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer yearLevel;
    private String major;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(Integer yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public StudentDto(String firstName, String lastName, String email, String password, Integer yearLevel, String major) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.yearLevel = yearLevel;
        this.major = major;
    }
    public StudentDto()
    {}

}
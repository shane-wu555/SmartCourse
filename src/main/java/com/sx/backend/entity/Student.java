package com.sx.backend.entity;

import java.util.List;

public class Student extends User{
    private String studentNumber; // 学号
    private String grade; // 年级
    private String major; // 专业
    private List<Course> enrolledCourse;

    public Student() {
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public List<Course> getEnrolledCourse() {
        return enrolledCourse;
    }

    public void setEnrolledCourse(List<Course> enrolledCourse) {
        this.enrolledCourse = enrolledCourse;
    }
}

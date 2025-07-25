package com.sx.backend.entity;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User{
    private String teacherId;
    private String employeeNumber; // 新增工号字段
    private String title;         // 职称
    private String department;    // 所属院系
    private String bio;           // 简介

    public Teacher(String userId, String username, String password, String email, String phone){
        super(userId, username, password, email, phone, Role.TEACHER);
    }

    public Teacher() {
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

}

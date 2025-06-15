package com.sx.backend.entity;

import java.util.ArrayList;
import java.util.List;

public class Teacher extends User{
    private String title;         // 职称
    private String department;    // 所属院系
    private String bio;           // 简介
    private List<String> taughtCourses = new ArrayList<>(); // 教授的课程ID列表

    public Teacher(String userId, String username, String password, String email, String phone){
        super(userId, username, password, email, phone, Role.TEACHER);
    }

    public Teacher() {
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

    public List<String> getTaughtCourses() {
        return taughtCourses;
    }

    public void setTaughtCourses(List<String> taughtCourses) {
        this.taughtCourses = taughtCourses;
    }
}

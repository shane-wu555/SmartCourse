package com.sx.backend.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private String realName; // 新增真实名字段
    private String role; // "TEACHER" 或 "STUDENT"

    // 学生特有字段
    private String studentNumber;
    private String grade;
    private String major;

    // 教师特有字段
    private String title;
    private String department;
    private String bio;

    // 无参构造
    public RegisterRequest() {}
}
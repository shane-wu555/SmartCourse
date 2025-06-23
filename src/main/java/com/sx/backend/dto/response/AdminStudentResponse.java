package com.sx.backend.dto.response;

import lombok.Data;

@Data
public class AdminStudentResponse {
    private String userId;
    private String username;
    private String realName;
    private String studentNumber;
    private String grade;
    private String major;
    private String email;
}
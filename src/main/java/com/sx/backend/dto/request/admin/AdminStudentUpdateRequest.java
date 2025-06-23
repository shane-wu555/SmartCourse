package com.sx.backend.dto.request.admin;

import lombok.Data;

@Data
public class AdminStudentUpdateRequest {
    private String realName;
    private String grade;
    private String major;
}
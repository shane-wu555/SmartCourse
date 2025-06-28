package com.sx.backend.dto.request.admin;

import lombok.Data;

@Data
public class AdminStudentQueryRequest {
    private String keyword;
    private String grade;
    private Integer page = 1;
    private Integer size = 10;
}
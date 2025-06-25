package com.sx.backend.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminStudentCreateRequest {
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "学号不能为空")
    private String studentNumber;

    private String grade;
    private String major;
}
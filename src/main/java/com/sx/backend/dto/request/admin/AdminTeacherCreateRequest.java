// AdminTeacherCreateRequest.java
package com.sx.backend.dto.request.admin;

import lombok.Data;

@Data
public class AdminTeacherCreateRequest {
    private String realName;
    private String employeeNumber;
    private String title;
    private String department;
    private String bio;
}
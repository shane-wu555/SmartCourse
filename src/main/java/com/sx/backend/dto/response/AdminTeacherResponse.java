// AdminTeacherResponse.java
package com.sx.backend.dto.response;

import lombok.Data;

@Data
public class AdminTeacherResponse {
    private String userId;
    private String username;
    private String realName;
    private String employeeNumber;
    private String title;
    private String department;
    private String bio;
    private String email;
}
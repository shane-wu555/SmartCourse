// AdminTeacherUpdateRequest.java
package com.sx.backend.dto.request.admin;

import lombok.Data;

@Data
public class AdminTeacherUpdateRequest {
    private String realName;
    private String title;
    private String department;
    private String bio;
}
// AdminTeacherQueryRequest.java
package com.sx.backend.dto.request.admin;

import lombok.Data;

@Data
public class AdminTeacherQueryRequest {
    private String keyword;
    private String department;
    private int page = 1;
    private int size = 10;
}
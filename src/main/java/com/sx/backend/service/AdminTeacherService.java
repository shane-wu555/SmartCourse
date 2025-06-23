// AdminTeacherService.java
package com.sx.backend.service;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminTeacherResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface AdminTeacherService {
    AdminTeacherResponse createTeacher(AdminTeacherCreateRequest request);
    Map<String, Object> importTeachers(MultipartFile file);
    Map<String, Object> getTeachers(AdminTeacherQueryRequest queryRequest);
    AdminTeacherResponse updateTeacher(String teacherId, AdminTeacherUpdateRequest request);
    void deleteTeacher(String teacherId);
}
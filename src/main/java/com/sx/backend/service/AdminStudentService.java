package com.sx.backend.service;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminStudentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AdminStudentService {
    AdminStudentResponse createStudent(AdminStudentCreateRequest request);
    Map<String, Object> batchCreateStudents(List<AdminStudentCreateRequest> requests);
    Map<String, Object> getStudents(AdminStudentQueryRequest queryRequest);
    AdminStudentResponse updateStudent(String studentId, AdminStudentUpdateRequest request);
    void deleteStudent(String studentId);
    Map<String, Object> importStudents(MultipartFile file);
}
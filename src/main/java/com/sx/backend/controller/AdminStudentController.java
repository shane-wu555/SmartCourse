package com.sx.backend.controller;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminStudentResponse;
import java.util.Map;

import com.sx.backend.exception.BusinessException;
import com.sx.backend.service.AdminStudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/student")
public class AdminStudentController {

    private final AdminStudentService adminStudentService;

    @Autowired
    public AdminStudentController(AdminStudentService adminStudentService) {
        this.adminStudentService = adminStudentService;
    }

    // 创建单个学生
    @PostMapping
    public ResponseEntity<?> createStudent(
            @RequestBody AdminStudentCreateRequest request) {
        try {
            AdminStudentResponse response = adminStudentService.createStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(Map.of(
                    "error", e.getMessage(),
                    "code", e.getCode()
            ));
        }
    }

    // Excel批量导入学生
    @PostMapping("/import")
    public ResponseEntity<?> importStudents(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = adminStudentService.importStudents(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(500, "批量导入失败: " + e.getMessage());
        }
    }

    // 分页查询学生
    @GetMapping("/students")
    public ResponseEntity<Map<String, Object>> getStudents(
            AdminStudentQueryRequest queryRequest) {
        Map<String, Object> result = adminStudentService.getStudents(queryRequest);
        return ResponseEntity.ok(result);
    }

    // 更新学生信息 - 改为按学号操作
    @PutMapping("/{studentNumber}")
    public ResponseEntity<AdminStudentResponse> updateStudent(
            @PathVariable String studentNumber, // 改为学号参数
            @RequestBody AdminStudentUpdateRequest request) {
        AdminStudentResponse response = adminStudentService.updateStudent(studentNumber, request);
        return ResponseEntity.ok(response);
    }

    // 删除学生 - 改为按学号操作
    @DeleteMapping("/{studentNumber}")
    public ResponseEntity<Void> deleteStudent(@PathVariable String studentNumber) { // 改为学号参数
        adminStudentService.deleteStudent(studentNumber);
        return ResponseEntity.noContent().build();
    }
}

// AdminTeacherController.java
package com.sx.backend.controller;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminTeacherResponse;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.service.AdminTeacherService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/teacher")
public class AdminTeacherController {

    private final AdminTeacherService adminTeacherService;

    @Autowired
    public AdminTeacherController(AdminTeacherService adminTeacherService) {
        this.adminTeacherService = adminTeacherService;
    }

    // 创建单个教师
    @PostMapping
    public ResponseEntity<?> createTeacher(
            @RequestBody AdminTeacherCreateRequest request) {
        try {
            AdminTeacherResponse response = adminTeacherService.createTeacher(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(Map.of(
                    "error", e.getMessage(),
                    "code", e.getCode()
            ));
        }
    }

    // Excel批量导入教师
    @PostMapping("/import")
    public ResponseEntity<?> importTeachers(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = adminTeacherService.importTeachers(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (BusinessException e) {
            return ResponseEntity.status(e.getCode()).body(Map.of(
                    "error", e.getMessage(),
                    "code", e.getCode()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "批量导入失败: " + e.getMessage(),
                    "code", 500
            ));
        }
    }

    // 分页查询教师
    @GetMapping("/teachers")
    public ResponseEntity<Map<String, Object>> getTeachers(
            AdminTeacherQueryRequest queryRequest) {
        Map<String, Object> result = adminTeacherService.getTeachers(queryRequest);
        return ResponseEntity.ok(result);
    }

    // 更新教师信息
    @PutMapping("/{employeeNumber}")
    public ResponseEntity<AdminTeacherResponse> updateTeacher(
            @PathVariable String employeeNumber,
            @RequestBody AdminTeacherUpdateRequest request) {
        AdminTeacherResponse response = adminTeacherService.updateTeacher(employeeNumber, request);
        return ResponseEntity.ok(response);
    }

    // 删除教师
    @DeleteMapping("/{employeeNumber}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable String employeeNumber) {
        adminTeacherService.deleteTeacher(employeeNumber);
        return ResponseEntity.noContent().build();
    }
}
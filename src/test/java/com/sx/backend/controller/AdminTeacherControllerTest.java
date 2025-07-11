package com.sx.backend.controller;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminTeacherResponse;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.service.AdminTeacherService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminTeacherControllerTest {

    @InjectMocks
    private AdminTeacherController adminTeacherController;

    @Mock
    private AdminTeacherService adminTeacherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTeacher_success() {
        AdminTeacherCreateRequest request = new AdminTeacherCreateRequest();
        AdminTeacherResponse response = new AdminTeacherResponse();

        when(adminTeacherService.createTeacher(request)).thenReturn(response);

        ResponseEntity<?> result = adminTeacherController.createTeacher(request);
        assertEquals(201, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }

    @Test
    void testCreateTeacher_businessException() {
        AdminTeacherCreateRequest request = new AdminTeacherCreateRequest();
        BusinessException ex = new BusinessException(400, "创建教师失败");

        when(adminTeacherService.createTeacher(request)).thenThrow(ex);

        ResponseEntity<?> result = adminTeacherController.createTeacher(request);
        assertEquals(400, result.getStatusCodeValue());
        assertTrue(((Map<?, ?>) result.getBody()).get("error").equals("创建教师失败"));
    }

    @Test
    void testImportTeachers_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "teachers.xlsx", "application/vnd.ms-excel", "test data".getBytes());
        Map<String, Object> mockResult = Map.of("count", 5);

        when(adminTeacherService.importTeachers(file)).thenReturn(mockResult);

        ResponseEntity<?> result = adminTeacherController.importTeachers(file);
        assertEquals(201, result.getStatusCodeValue());
        assertEquals(mockResult, result.getBody());
    }

    @Test
    void testImportTeachers_businessException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "teachers.xlsx", "application/vnd.ms-excel", "test data".getBytes());
        BusinessException ex = new BusinessException(409, "导入失败");

        when(adminTeacherService.importTeachers(file)).thenThrow(ex);

        ResponseEntity<?> result = adminTeacherController.importTeachers(file);
        assertEquals(409, result.getStatusCodeValue());
        assertEquals("导入失败", ((Map<?, ?>) result.getBody()).get("error"));
    }

    @Test
    void testImportTeachers_generalException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "teachers.xlsx", "application/vnd.ms-excel", "test data".getBytes());

        when(adminTeacherService.importTeachers(file)).thenThrow(new RuntimeException("IO错误"));

        ResponseEntity<?> result = adminTeacherController.importTeachers(file);
        assertEquals(500, result.getStatusCodeValue());
        assertTrue(((Map<?, ?>) result.getBody()).get("error").toString().contains("批量导入失败"));
    }

    @Test
    void testGetTeachers() {
        AdminTeacherQueryRequest queryRequest = new AdminTeacherQueryRequest();
        Map<String, Object> mockResult = Map.of("total", 12);

        when(adminTeacherService.getTeachers(queryRequest)).thenReturn(mockResult);

        ResponseEntity<Map<String, Object>> result = adminTeacherController.getTeachers(queryRequest);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(12, result.getBody().get("total"));
    }

    @Test
    void testUpdateTeacher() {
        String employeeNumber = "T12345";
        AdminTeacherUpdateRequest request = new AdminTeacherUpdateRequest();
        AdminTeacherResponse response = new AdminTeacherResponse();

        when(adminTeacherService.updateTeacher(employeeNumber, request)).thenReturn(response);

        ResponseEntity<AdminTeacherResponse> result = adminTeacherController.updateTeacher(employeeNumber, request);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }

    @Test
    void testDeleteTeacher() {
        String employeeNumber = "T12345";

        ResponseEntity<Void> result = adminTeacherController.deleteTeacher(employeeNumber);

        verify(adminTeacherService, times(1)).deleteTeacher(employeeNumber);
        assertEquals(204, result.getStatusCodeValue());
    }
}

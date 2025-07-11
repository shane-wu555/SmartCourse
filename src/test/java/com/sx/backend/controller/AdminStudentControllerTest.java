package com.sx.backend.controller;

import com.sx.backend.dto.request.admin.*;
import com.sx.backend.dto.response.AdminStudentResponse;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.service.AdminStudentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminStudentControllerTest {

    @InjectMocks
    private AdminStudentController adminStudentController;

    @Mock
    private AdminStudentService adminStudentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateStudent_success() {
        AdminStudentCreateRequest request = new AdminStudentCreateRequest();
        AdminStudentResponse response = new AdminStudentResponse();

        when(adminStudentService.createStudent(request)).thenReturn(response);

        ResponseEntity<?> result = adminStudentController.createStudent(request);
        assertEquals(201, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }

    @Test
    void testCreateStudent_businessException() {
        AdminStudentCreateRequest request = new AdminStudentCreateRequest();
        BusinessException ex = new BusinessException(400, "创建失败");

        when(adminStudentService.createStudent(request)).thenThrow(ex);

        ResponseEntity<?> result = adminStudentController.createStudent(request);
        assertEquals(400, result.getStatusCodeValue());
        assertTrue(((Map<?, ?>) result.getBody()).get("error").equals("创建失败"));
    }

    @Test
    void testImportStudents_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "data".getBytes());
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        when(adminStudentService.importStudents(file)).thenReturn(response);

        ResponseEntity<?> result = adminStudentController.importStudents(file);
        assertEquals(201, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }

    @Test
    void testImportStudents_genericException() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "data".getBytes());

        try {
            when(adminStudentService.importStudents(file)).thenThrow(new RuntimeException("IO失败"));
            adminStudentController.importStudents(file);
            fail("应抛出BusinessException");
        } catch (BusinessException e) {
            assertEquals(500, e.getCode());
            assertTrue(e.getMessage().contains("批量导入失败"));
        }
    }

    @Test
    void testGetStudents() {
        AdminStudentQueryRequest queryRequest = new AdminStudentQueryRequest();
        Map<String, Object> resultData = new HashMap<>();
        resultData.put("total", 10);

        when(adminStudentService.getStudents(queryRequest)).thenReturn(resultData);

        ResponseEntity<Map<String, Object>> result = adminStudentController.getStudents(queryRequest);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(10, result.getBody().get("total"));
    }

    @Test
    void testUpdateStudent() {
        String studentNumber = "20230101";
        AdminStudentUpdateRequest request = new AdminStudentUpdateRequest();
        AdminStudentResponse response = new AdminStudentResponse();

        when(adminStudentService.updateStudent(studentNumber, request)).thenReturn(response);

        ResponseEntity<AdminStudentResponse> result = adminStudentController.updateStudent(studentNumber, request);
        assertEquals(200, result.getStatusCodeValue());
        assertEquals(response, result.getBody());
    }

    @Test
    void testDeleteStudent() {
        String studentNumber = "20230101";

        // doNothing is default, just verify invocation
        ResponseEntity<Void> result = adminStudentController.deleteStudent(studentNumber);

        verify(adminStudentService, times(1)).deleteStudent(studentNumber);
        assertEquals(204, result.getStatusCodeValue());
    }
}

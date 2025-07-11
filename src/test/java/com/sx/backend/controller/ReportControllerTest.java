package com.sx.backend.controller;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportControllerTest {

    @InjectMocks
    private ReportController reportController;

    @Mock
    private ReportService reportService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCourseReport_success() {
        AnalysisReportDTO dto = new AnalysisReportDTO();
        when(reportService.generateCourseReport("c1")).thenReturn(dto);

        ResponseEntity<AnalysisReportDTO> result = reportController.getCourseReport("c1");

        assertEquals(200, result.getStatusCodeValue());
        assertEquals(dto, result.getBody());
    }

    @Test
    void testGetCourseReport_exception() {
        when(reportService.generateCourseReport("c2")).thenThrow(new RuntimeException("fail"));

        assertThrows(RuntimeException.class, () -> reportController.getCourseReport("c2"));
    }

    @Test
    void testExportReport_success() {
        doNothing().when(reportService).exportGradeReport("c1", response);

        assertDoesNotThrow(() -> reportController.exportReport("c1", response));
        verify(reportService, times(1)).exportGradeReport("c1", response);
    }

    @Test
    void testExportReport_exception() throws IOException {
        doThrow(new RuntimeException("export error")).when(reportService).exportGradeReport("c2", response);

        assertThrows(RuntimeException.class, () -> reportController.exportReport("c2", response));
    }
}
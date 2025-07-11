package com.sx.backend.controller;

import com.sx.backend.dto.GradeTrendDTO;
import com.sx.backend.service.AnalysisService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AnalysisControllerTest {

    @InjectMocks
    private AnalysisController analysisController;

    @Mock
    private AnalysisService analysisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetGradeTrend_success() {
        String studentId = "S123";
        String courseId = "C456";

        GradeTrendDTO trend = new GradeTrendDTO();
        trend.setDates(List.of("2023-10-01", "2023-10-08"));
        trend.setScores(List.of(85.0f, 90.0f));
        trend.setTotalScores(List.of(100.0f, 100.0f));
        trend.setTaskNames(List.of("作业1", "测验1"));

        doNothing().when(analysisService).updateGradeTrend(studentId, courseId);
        when(analysisService.getGradeTrend(studentId, courseId)).thenReturn(trend);

        ResponseEntity<GradeTrendDTO> response = analysisController.getGradeTrend(studentId, courseId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(trend, response.getBody());
        assertEquals(2, response.getBody().getDates().size());
        assertEquals(List.of(85.0f, 90.0f), response.getBody().getScores());

        verify(analysisService, times(1)).updateGradeTrend(studentId, courseId);
        verify(analysisService, times(1)).getGradeTrend(studentId, courseId);
    }
}

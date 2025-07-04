package com.sx.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.GradeTrendDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.util.ChartGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private ChartGenerator chartGenerator;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    @Test
    void updateGradeTrend_WhenGradeNotFound_ShouldDoNothing() {
        // 准备
        when(gradeMapper.findByStudentAndCourse(anyString(), anyString())).thenReturn(null);

        // 执行
        analysisService.updateGradeTrend("S001", "C001");

        // 验证
        verify(taskGradeMapper, never()).findByStudentAndCourse(anyString(), anyString());
        verify(gradeMapper, never()).update(any());
    }

    @Test
    void updateGradeTrend_WithValidData_ShouldUpdateGrade() throws Exception {
        // 准备
        Grade grade = new Grade();
        when(gradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(grade);

        List<TaskGrade> taskGrades = Arrays.asList(
                createTaskGrade("1", 85.5f, LocalDateTime.now().minusDays(2)),
                createTaskGrade("2", 92.0f, LocalDateTime.now())
        );
        when(taskGradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(taskGrades);
        when(chartGenerator.generateLineChart(any(), any())).thenReturn("chart_base64");

        // 执行
        analysisService.updateGradeTrend("S001", "C001");

        // 验证
        verify(gradeMapper).update(grade);
        assertNotNull(grade.getGradeTrend());
        assertTrue(grade.getGradeTrend().contains("chartImage"));
    }

    @Test
    void updateGradeTrend_WithEmptyTaskGrades_ShouldGenerateEmptyArrays() throws JsonProcessingException {
        // 准备
        Grade grade = new Grade();
        when(gradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(grade);
        when(taskGradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(Collections.emptyList());

        // 执行
        analysisService.updateGradeTrend("S001", "C001");

        // 验证
        verify(gradeMapper).update(grade);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode trend = mapper.readTree(grade.getGradeTrend());
        assertTrue(trend.get("dates").isEmpty());
        assertTrue(trend.get("scores").isEmpty());
    }

    @Test
    void getGradeTrend_WhenGradeNotFound_ShouldReturnNull() {
        // 准备
        when(gradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(null);

        // 执行
        GradeTrendDTO result = analysisService.getGradeTrend("S001", "C001");

        // 验证
        assertNull(result);
    }

    @Test
    void getGradeTrend_WhenTrendDataExists_ShouldReturnDTO() throws Exception {
        // 准备
        Grade grade = new Grade();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode trendData = mapper.createObjectNode();

        ArrayNode dates = trendData.putArray("dates");
        dates.add("2023-01-01");
        dates.add("2023-01-02");

        ArrayNode scores = trendData.putArray("scores");
        scores.add(85.5);
        scores.add(92.0);

        grade.setGradeTrend(trendData.toString());
        when(gradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(grade);

        // 执行
        GradeTrendDTO result = analysisService.getGradeTrend("S001", "C001");

        // 验证
        assertNotNull(result);
        assertEquals(2, result.getDates().size());
        assertEquals("2023-01-01", result.getDates().get(0));
        assertEquals(85.5f, result.getScores().get(0));
    }

    @Test
    void getGradeTrend_WithInvalidJson_ShouldReturnNull() {
        // 准备
        Grade grade = new Grade();
        grade.setGradeTrend("invalid_json");
        when(gradeMapper.findByStudentAndCourse("S001", "C001")).thenReturn(grade);

        // 执行
        GradeTrendDTO result = analysisService.getGradeTrend("S001", "C001");

        // 验证
        assertNull(result);
    }

    // 辅助方法：创建TaskGrade对象
    private TaskGrade createTaskGrade(String id, float score, LocalDateTime time) {
        TaskGrade tg = new TaskGrade();
        tg.setTaskId(id);
        tg.setScore(score);
        tg.setGradedTime(time);
        return tg;
    }
}
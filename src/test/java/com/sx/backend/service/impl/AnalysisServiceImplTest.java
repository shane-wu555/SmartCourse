package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class AnalysisServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private ChartGenerator chartGenerator;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    private final String STUDENT_ID = "student123";
    private final String COURSE_ID = "course456";
    private final String GRADE_ID = "grade789";

    private Grade createTestGrade() {
        Grade grade = new Grade();
        grade.setGradeId(GRADE_ID);
        grade.setStudentId(STUDENT_ID);
        grade.setCourseId(COURSE_ID);
        return grade;
    }

    private List<TaskGrade> createTaskGrades() {
        TaskGrade tg1 = new TaskGrade();
        tg1.setScore(85.0f);
        tg1.setSubmissionTime(LocalDateTime.now().minusDays(7));

        TaskGrade tg2 = new TaskGrade();
        tg2.setScore(92.0f);
        tg2.setSubmissionTime(LocalDateTime.now().minusDays(3));

        TaskGrade tg3 = new TaskGrade();
        tg3.setScore(88.0f);
        tg3.setSubmissionTime(LocalDateTime.now());

        return Arrays.asList(tg1, tg2, tg3);
    }

    @Test
    void updateGradeTrend_GradeNotFound_NoAction() {
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(null);

        analysisService.updateGradeTrend(STUDENT_ID, COURSE_ID);

        verify(gradeMapper, never()).update(any());
    }

    @Test
    void updateGradeTrend_NoTaskGrades_NoAction() {
        Grade grade = createTestGrade();
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId(GRADE_ID)).thenReturn(Collections.emptyList());

        analysisService.updateGradeTrend(STUDENT_ID, COURSE_ID);

        verify(gradeMapper, never()).update(any());
    }

    @Test
    void updateGradeTrend_Success() throws Exception {
        // 准备测试数据
        Grade grade = createTestGrade();
        List<TaskGrade> taskGrades = createTaskGrades();

        // 模拟依赖行为
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId(GRADE_ID)).thenReturn(taskGrades);
        when(chartGenerator.generateLineChart(anyList(), anyList())).thenReturn("base64_chart_image");

        // 执行方法
        analysisService.updateGradeTrend(STUDENT_ID, COURSE_ID);

        // 验证结果
        verify(gradeMapper).update(grade);
        assertNotNull(grade.getGradeTrend());

        // 验证生成的JSON结构
        ObjectNode trendData = (ObjectNode) objectMapper.readTree(grade.getGradeTrend());
        assertTrue(trendData.has("dates"));
        assertTrue(trendData.has("scores"));
        assertTrue(trendData.has("chartImage"));
        assertEquals("base64_chart_image", trendData.get("chartImage").asText());

        // 验证日期排序
        ArrayNode dates = (ArrayNode) trendData.get("dates");
        String firstDate = dates.get(0).asText();
        String lastDate = dates.get(dates.size() - 1).asText();
        assertTrue(firstDate.compareTo(lastDate) < 0); // 确保日期升序排列
    }

    @Test
    void getGradeTrend_GradeNotFound_ReturnsNull() {
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(null);

        GradeTrendDTO result = analysisService.getGradeTrend(STUDENT_ID, COURSE_ID);

        assertNull(result);
    }

    @Test
    void getGradeTrend_GradeTrendNull_ReturnsNull() {
        Grade grade = createTestGrade();
        grade.setGradeTrend(null);

        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);

        GradeTrendDTO result = analysisService.getGradeTrend(STUDENT_ID, COURSE_ID);

        assertNull(result);
    }

    @Test
    void getGradeTrend_InvalidJson_ReturnsNull() {
        Grade grade = createTestGrade();
        grade.setGradeTrend("invalid_json");

        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);

        GradeTrendDTO result = analysisService.getGradeTrend(STUDENT_ID, COURSE_ID);

        assertNull(result);
    }

    @Test
    void getGradeTrend_Success() throws Exception {
        // 准备测试数据
        Grade grade = createTestGrade();

        // 创建趋势数据JSON
        ObjectNode trendData = objectMapper.createObjectNode();
        ArrayNode dates = trendData.putArray("dates");
        dates.add("2023-10-01");
        dates.add("2023-10-15");
        dates.add("2023-10-30");

        ArrayNode scores = trendData.putArray("scores");
        scores.add(85.5);
        scores.add(92.0);
        scores.add(88.5);

        grade.setGradeTrend(trendData.toString());

        // 模拟依赖
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);

        // 执行方法
        GradeTrendDTO result = analysisService.getGradeTrend(STUDENT_ID, COURSE_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.getDates().size());
        assertEquals("2023-10-01", result.getDates().get(0));
        assertEquals(3, result.getScores().size());
        assertEquals(85.5f, result.getScores().get(0), 0.01);
    }

    @Test
    void updateGradeTrend_ChartGeneratorException_StillUpdates() {
        // 准备测试数据
        Grade grade = createTestGrade();
        List<TaskGrade> taskGrades = createTaskGrades();

        // 模拟依赖行为
        when(gradeMapper.findByStudentAndCourse(STUDENT_ID, COURSE_ID)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId(GRADE_ID)).thenReturn(taskGrades);
        when(chartGenerator.generateLineChart(anyList(), anyList())).thenThrow(new RuntimeException("Chart error"));

        // 执行方法
        analysisService.updateGradeTrend(STUDENT_ID, COURSE_ID);

        // 验证结果 - 即使图表生成失败，其他数据仍然更新
        verify(gradeMapper).update(grade);
        assertNotNull(grade.getGradeTrend());
        assertFalse(grade.getGradeTrend().contains("chartImage")); // 图表字段可能不存在
    }
}

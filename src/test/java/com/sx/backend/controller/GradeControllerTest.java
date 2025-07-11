package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.AnalysisService;
import com.sx.backend.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GradeControllerTest {

    private GradeController controller;
    private GradeService gradeService;
    private GradeMapper gradeMapper;
    private AnalysisService analysisService;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        gradeService = mock(GradeService.class);
        gradeMapper = mock(GradeMapper.class);
        analysisService = mock(AnalysisService.class);
        request = mock(HttpServletRequest.class);

        controller = new GradeController( request );
        setField(controller, "gradeService", gradeService);
        setField(controller, "gradeMapper", gradeMapper);
        setField(controller, "analysisService", analysisService);
    }

    @Test
    void testGetGradesByStudentId() {
        when(request.getAttribute("userId")).thenReturn("stu1");
        Grade g = new Grade();
        when(gradeMapper.findByStudentId("stu1")).thenReturn(List.of(g));

        ResponseEntity<List<Grade>> resp = controller.getGradesByStudentId();
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void testGetGradesByCourseId() {
        Grade g = new Grade();
        when(gradeMapper.findByCourseId("c1")).thenReturn(List.of(g));

        ResponseEntity<List<Grade>> resp = controller.getGradesByCourseId("c1");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void testGetGrade_found() {
        when(request.getAttribute("userId")).thenReturn("stu1");
        Grade g = new Grade();
        when(gradeMapper.findByStudentAndCourse("stu1", "c1")).thenReturn(g);

        ResponseEntity<Grade> resp = controller.getGrade("c1");
        assertEquals(200, resp.getStatusCodeValue());
        assertSame(g, resp.getBody());
        verify(analysisService).updateGradeTrend("stu1", "c1");
    }

    @Test
    void testGetGrade_notFound() {
        when(request.getAttribute("userId")).thenReturn("stu1");
        when(gradeMapper.findByStudentAndCourse("stu1", "c1")).thenReturn(null);

        ResponseEntity<Grade> resp = controller.getGrade("c1");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("stu1", resp.getBody().getStudentId());
        assertEquals("c1", resp.getBody().getCourseId());
        assertEquals(Collections.emptyList(), resp.getBody().getTaskGrades());
        verify(analysisService).updateGradeTrend("stu1", "c1");
    }

    // 反射工具方法
    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
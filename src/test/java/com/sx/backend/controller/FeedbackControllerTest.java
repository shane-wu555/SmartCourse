package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.FeedbackService;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeedbackControllerTest {

    @InjectMocks
    private FeedbackController feedbackController;

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        feedbackController = new FeedbackController(request); // 只注入构造参数

        // 手动注入其余字段（模拟@Autowired注入效果）
        org.springframework.test.util.ReflectionTestUtils.setField(feedbackController, "feedbackService", feedbackService);
        org.springframework.test.util.ReflectionTestUtils.setField(feedbackController, "gradeMapper", gradeMapper);
    }

    @Test
    void testGetFeedback_success() {
        String courseId = "COURSE123";
        String studentId = "STUDENT001";

        when(request.getAttribute("userId")).thenReturn(studentId);
        Grade grade = new Grade();
        grade.setFeedback("你的表现很好，继续加油！");
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);

        ResponseEntity<String> response = feedbackController.getFeedback(courseId);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("你的表现很好，继续加油！", response.getBody());

        verify(feedbackService).generateFeedback(studentId, courseId);
    }

    @Test
    void testGetFeedback_notFound() {
        String courseId = "COURSE999";
        String studentId = "STUDENT002";

        when(request.getAttribute("userId")).thenReturn(studentId);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null);

        ResponseEntity<String> response = feedbackController.getFeedback(courseId);

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(feedbackService).generateFeedback(studentId, courseId);
    }

    @Test
    void testGetFeedback_noContent() {
        String courseId = "COURSE888";
        String studentId = "STUDENT003";

        Grade grade = new Grade();
        grade.setFeedback(""); // 空反馈

        when(request.getAttribute("userId")).thenReturn(studentId);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);

        ResponseEntity<String> response = feedbackController.getFeedback(courseId);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());

        verify(feedbackService).generateFeedback(studentId, courseId);
    }
}

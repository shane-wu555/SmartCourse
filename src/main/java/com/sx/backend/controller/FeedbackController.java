package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final HttpServletRequest request;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private GradeMapper gradeMapper;


    private String getCurrentStudentId() {
        // 从请求属性中获取拦截器设置的userId
        return (String) request.getAttribute("userId");
    }

    /**
     * 获取学生的课程反馈
     *
     * @param courseId  课程ID
     * @return 学生的课程反馈
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<String> getFeedback(
            @PathVariable String courseId) {

        String studentId = getCurrentStudentId();

        feedbackService.generateFeedback(studentId, courseId);

        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) {
            return ResponseEntity.notFound().build();
        }
        String feedback = grade.getFeedback();
        if (feedback == null || feedback.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(feedback);
    }
}

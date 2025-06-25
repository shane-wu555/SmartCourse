package com.sx.backend.controller;

import com.sx.backend.dto.FeedbackDTO;
import com.sx.backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 获取学生的课程反馈
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 学生的课程反馈
     */
    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<FeedbackDTO> getFeedback(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        FeedbackDTO feedback = feedbackService.generateFeedback(studentId, courseId);
        return ResponseEntity.ok(feedback);
    }
}

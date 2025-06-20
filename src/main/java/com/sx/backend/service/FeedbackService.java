package com.sx.backend.service;

import com.sx.backend.dto.FeedbackDTO;

public interface FeedbackService {
    /**
     * 生成学习反馈
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 反馈DTO
     */
    FeedbackDTO generateFeedback(String studentId, String courseId);
}

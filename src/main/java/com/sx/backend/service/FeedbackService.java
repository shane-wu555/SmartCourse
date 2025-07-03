package com.sx.backend.service;

public interface FeedbackService {
    /**
     * 生成学习反馈
     * @param studentId 学生ID
     * @param courseId 课程ID
     */
    void generateFeedback(String studentId, String courseId);
}

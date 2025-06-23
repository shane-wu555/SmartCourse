package com.sx.backend.service;

import com.sx.backend.dto.GradeTrendDTO;

public interface AnalysisService {
    /**
     * 更新成绩趋势数据
     * @param studentId 学生ID
     * @param courseId 课程ID
     */
    void updateGradeTrend(String studentId, String courseId);

    /**
     * 获取成绩趋势数据
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩趋势DTO
     */
    GradeTrendDTO getGradeTrend(String studentId, String courseId);
}

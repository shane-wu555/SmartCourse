package com.sx.backend.service;

import com.sx.backend.dto.AnalysisReportDTO;
import jakarta.servlet.http.HttpServletResponse;

public interface ReportService {
    /**
     * 生成课程分析报告
     * @param courseId 课程ID
     * @return 分析报告DTO
     */
    AnalysisReportDTO generateCourseReport(String courseId);

    /**
     * 导出成绩报表
     * @param courseId 课程ID
     * @param response HTTP响应对象
     */
    void exportGradeReport(String courseId, HttpServletResponse response);
}

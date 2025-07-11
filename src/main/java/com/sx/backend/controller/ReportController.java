package com.sx.backend.controller;

import com.sx.backend.dto.AnalysisReportDTO;
import com.sx.backend.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // 获取学生的课程分析报告
    @GetMapping("/course/{courseId}")
    public ResponseEntity<AnalysisReportDTO> getCourseReport(
            @PathVariable String courseId) {

        AnalysisReportDTO report = reportService.generateCourseReport(courseId);
        return ResponseEntity.ok(report);
    }

    // 导出课程成绩报表
    @GetMapping("/export/{courseId}")
    public void exportReport(
            @PathVariable String courseId,
            HttpServletResponse response) {
        
        // 设置CORS头
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition, Content-Length");
        
        try {
            reportService.exportGradeReport(courseId, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"导出失败: " + e.getMessage() + "\"}");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

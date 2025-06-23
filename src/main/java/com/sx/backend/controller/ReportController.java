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

    @GetMapping("/course/{courseId}")
    public ResponseEntity<AnalysisReportDTO> getCourseReport(
            @PathVariable String courseId) {

        AnalysisReportDTO report = reportService.generateCourseReport(courseId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export/{courseId}")
    public void exportReport(
            @PathVariable String courseId,
            HttpServletResponse response) {

        reportService.exportGradeReport(courseId, response);
    }
}

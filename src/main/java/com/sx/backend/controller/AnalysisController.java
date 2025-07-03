package com.sx.backend.controller;

import com.sx.backend.dto.GradeTrendDTO;
import com.sx.backend.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    @Autowired
    private AnalysisService analysisService;

    // 获取学生在特定课程的成绩趋势
    @GetMapping("/trend/{studentId}/{courseId}")
    public ResponseEntity<GradeTrendDTO> getGradeTrend(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        analysisService.updateGradeTrend(studentId, courseId);
        GradeTrendDTO trend = analysisService.getGradeTrend(studentId, courseId);
        return ResponseEntity.ok(trend);
    }
}

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

    @GetMapping("/trend/{studentId}/{courseId}")
    public ResponseEntity<GradeTrendDTO> getGradeTrend(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        GradeTrendDTO trend = analysisService.getGradeTrend(studentId, courseId);
        return ResponseEntity.ok(trend);
    }
}

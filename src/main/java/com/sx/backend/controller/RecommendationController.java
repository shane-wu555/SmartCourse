package com.sx.backend.controller;

import com.sx.backend.dto.RecommendationRequest;
import com.sx.backend.dto.RecommendationResponse;
import com.sx.backend.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * AI推荐控制器
 */
@RestController
@RequestMapping("/api/recommendation")
@Slf4j
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        return (String) request.getAttribute("userId");
    }
    
    /**
     * 生成学习推荐
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<RecommendationResponse>> generateRecommendation(
            @RequestBody RecommendationRequest recommendationRequest) {
        try {
            log.info("生成学习推荐请求: {}", recommendationRequest);
            
            // 设置当前用户ID
            String currentUserId = getCurrentUserId();
            recommendationRequest.setStudentId(currentUserId);
            
            RecommendationResponse response = recommendationService.generateRecommendation(recommendationRequest);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "推荐生成成功",
                    response
            ));
            
        } catch (Exception e) {
            log.error("生成推荐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "推荐生成失败: " + e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * 获取知识点推荐
     */
    @GetMapping("/knowledge-points")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getKnowledgePointRecommendations(
            @RequestParam String courseId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            String studentId = getCurrentUserId();
            log.info("获取知识点推荐，学生ID: {}, 课程ID: {}, 限制: {}", studentId, courseId, limit);
            
            RecommendationResponse response = recommendationService.getKnowledgePointRecommendations(
                    studentId, courseId, limit);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "知识点推荐获取成功",
                    response
            ));
            
        } catch (Exception e) {
            log.error("获取知识点推荐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "获取知识点推荐失败: " + e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * 获取资源推荐
     */
    @GetMapping("/resources")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getResourceRecommendations(
            @RequestParam String courseId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            String studentId = getCurrentUserId();
            log.info("获取资源推荐，学生ID: {}, 课程ID: {}, 限制: {}", studentId, courseId, limit);
            
            RecommendationResponse response = recommendationService.getResourceRecommendations(
                    studentId, courseId, limit);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "资源推荐获取成功",
                    response
            ));
            
        } catch (Exception e) {
            log.error("获取资源推荐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "获取资源推荐失败: " + e.getMessage(),
                            null
                    ));
        }
    }
    
    /**
     * 获取综合推荐
     */
    @GetMapping("/comprehensive")
    public ResponseEntity<ApiResponse<RecommendationResponse>> getComprehensiveRecommendations(
            @RequestParam String courseId) {
        try {
            String studentId = getCurrentUserId();
            log.info("获取综合推荐，学生ID: {}, 课程ID: {}", studentId, courseId);
            
            RecommendationResponse response = recommendationService.getComprehensiveRecommendations(
                    studentId, courseId);
            
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK.value(),
                    "综合推荐获取成功",
                    response
            ));
            
        } catch (Exception e) {
            log.error("获取综合推荐失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "获取综合推荐失败: " + e.getMessage(),
                            null
                    ));
        }
    }
}

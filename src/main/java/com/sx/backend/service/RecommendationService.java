package com.sx.backend.service;

import com.sx.backend.dto.RecommendationRequest;
import com.sx.backend.dto.RecommendationResponse;

/**
 * AI推荐服务接口
 */
public interface RecommendationService {
    
    /**
     * 生成学习推荐
     * @param request 推荐请求
     * @return 推荐响应
     */
    RecommendationResponse generateRecommendation(RecommendationRequest request);
    
    /**
     * 获取知识点推荐
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param limit 推荐数量限制
     * @return 推荐响应
     */
    RecommendationResponse getKnowledgePointRecommendations(String studentId, String courseId, int limit);
    
    /**
     * 获取资源推荐
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param limit 推荐数量限制
     * @return 推荐响应
     */
    RecommendationResponse getResourceRecommendations(String studentId, String courseId, int limit);
    
    /**
     * 获取综合学习建议
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 推荐响应
     */
    RecommendationResponse getComprehensiveRecommendations(String studentId, String courseId);
}

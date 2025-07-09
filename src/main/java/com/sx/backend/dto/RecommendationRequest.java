package com.sx.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * AI推荐请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 推荐类型：knowledge_point（知识点推荐）、resource（资源推荐）
     */
    private String type;
    
    /**
     * 推荐数量限制（可选，默认为5）
     */
    private Integer limit = 5;
    
    /**
     * 最低成绩阈值（可选，用于筛选需要提升的知识点）
     */
    private Float minScoreThreshold = 60.0f;
}

package com.sx.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    /**
     * 学生ID
     */
    private String studentId;
    
    /**
     * 课程ID
     */
    private String courseId;
    
    /**
     * 推荐类型
     */
    private String type;
    
    /**
     * 知识点推荐列表
     */
    private List<KnowledgePointRecommendation> knowledgePointRecommendations;
    
    /**
     * 资源推荐列表
     */
    private List<ResourceRecommendation> resourceRecommendations;
    
    /**
     * 总体学习建议
     */
    private String overallSuggestion;
    
    /**
     * 学生当前整体成绩
     */
    private Float currentGrade;
    
    /**
     * 学生在班级中的排名
     */
    private Integer classRank;
    
    /**
     * 推荐生成时间
     */
    private String generatedTime;
    
    /**
     * AI分析的学习状态
     */
    private String learningStatus;
    
    /**
     * 推荐的学习路径
     */
    private String learningPath;
    
    /**
     * 预计提升空间
     */
    private Float expectedImprovement;
}

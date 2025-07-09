package com.sx.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 知识点推荐DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgePointRecommendation {
    /**
     * 知识点ID
     */
    private String pointId;
    
    /**
     * 知识点名称
     */
    private String name;
    
    /**
     * 知识点描述
     */
    private String description;
    
    /**
     * 难度级别
     */
    private String difficultyLevel;
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 推荐优先级（1-10，数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 学生在该知识点的当前掌握程度（0-100）
     */
    private Float masteryLevel;
    
    /**
     * 关联的资源数量
     */
    private Integer resourceCount;
    
    /**
     * 是否为薄弱知识点
     */
    private Boolean isWeakPoint;
}

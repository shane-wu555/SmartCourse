package com.sx.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 资源推荐DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRecommendation {
    /**
     * 资源ID
     */
    private String resourceId;
    
    /**
     * 资源名称
     */
    private String name;
    
    /**
     * 资源类型
     */
    private String type;
    
    /**
     * 资源URL
     */
    private String url;
    
    /**
     * 资源描述
     */
    private String description;
    
    /**
     * 推荐理由
     */
    private String reason;
    
    /**
     * 推荐优先级（1-10，数字越大优先级越高）
     */
    private Integer priority;
    
    /**
     * 关联的知识点ID
     */
    private String relatedKnowledgePointId;
    
    /**
     * 关联的知识点名称
     */
    private String relatedKnowledgePointName;
    
    /**
     * 资源大小
     */
    private Long size;
    
    /**
     * 资源时长（对于视频资源）
     */
    private Float duration;
    
    /**
     * 观看次数
     */
    private Integer viewCount;
    
    /**
     * 是否为重点推荐
     */
    private Boolean isHighPriority;
}

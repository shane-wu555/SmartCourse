package com.sx.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeatmapSegmentDTO {
    /**
     * 片段开始时间（秒）
     */
    private Double start;
    
    /**
     * 片段结束时间（秒）
     */
    private Double end;
    
    /**
     * 观看次数
     */
    private Integer count;
    
    /**
     * 热度强度（0-1之间）
     */
    private Double intensity;
    
    /**
     * 片段时长（秒）
     */
    private Double duration;
}

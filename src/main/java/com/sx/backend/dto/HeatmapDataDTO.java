package com.sx.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HeatmapDataDTO {
    /**
     * 视频总时长（秒）
     */
    private Double duration;
    
    /**
     * 数据版本
     */
    private Integer version;
    
    /**
     * 热力图片段数据
     */
    private List<HeatmapSegmentDTO> segments;
    
    /**
     * 总片段数
     */
    private Integer totalSegments;
    
    /**
     * 最大观看次数
     */
    private Integer maxCount;
    
    /**
     * 热力图统计信息
     */
    private HeatmapStatsDTO stats;
}

package com.sx.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeatmapStatsDTO {
    /**
     * 总观看时长（秒）
     */
    private Double totalWatchTime;
    
    /**
     * 平均观看次数
     */
    private Double averageWatchCount;
    
    /**
     * 最热点片段开始时间
     */
    private Double hottestSegmentStart;
    
    /**
     * 最热点片段结束时间
     */
    private Double hottestSegmentEnd;
    
    /**
     * 最热点片段观看次数
     */
    private Integer hottestSegmentCount;
    
    /**
     * 观看覆盖率（观看过的时间占总时长的百分比）
     */
    private Double coverageRate;
}

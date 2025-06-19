package com.sx.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class VideoProgressReportDTO {
    private String resourceId;
    private String resourceName;
    private Float completionRate;
    private Float totalWatchedMinutes;
    private LocalDateTime lastWatchTime;
    private Map<String, Integer> heatmap;
    private List<VideoSegment> rewatchedSections;
    private List<VideoSegment> skippedSections;
}

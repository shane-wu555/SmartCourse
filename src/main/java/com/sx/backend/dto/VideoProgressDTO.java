package com.sx.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VideoProgressDTO {
    private String progressId;
    private String resourceId;
    private String resourceName;
    private Float lastPosition;
    private Float totalWatched;
    private Float completionRate;
    private String heatmapData;
    private LocalDateTime lastWatchTime;
}

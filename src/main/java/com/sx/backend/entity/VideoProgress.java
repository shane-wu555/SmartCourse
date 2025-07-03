package com.sx.backend.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VideoProgress {
    private String progressId;
    private Resource video;
    private String userId;
    private Float lastPosition;
    private Float totalWatched;
    private Float completionRate;
    private String heatmapData;
    private LocalDateTime lastWatchTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   public VideoProgress() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    public VideoProgress(String progressId, Resource video, String userId, Float lastPosition, Float totalWatched, Float completionRate, String heatmapData, LocalDateTime lastWatchTime, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.progressId = progressId;
        this.video = video;
        this.userId = userId;
        this.lastPosition = lastPosition;
        this.totalWatched = totalWatched;
        this.completionRate = completionRate;
        this.heatmapData = heatmapData;
        this.lastWatchTime = lastWatchTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

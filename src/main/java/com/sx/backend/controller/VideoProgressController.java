package com.sx.backend.controller;

import com.sx.backend.dto.VideoProgressDTO;
import com.sx.backend.dto.VideoProgressReportDTO;
import com.sx.backend.dto.HeatmapDataDTO;
import com.sx.backend.dto.request.UpdateProgressRequest;
import com.sx.backend.entity.VideoProgress;
import com.sx.backend.service.VideoProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
//TODO 相关接口测试并未验证
@RestController
@RequestMapping("/api/video-progress")
@RequiredArgsConstructor
public class VideoProgressController {
    private final VideoProgressService videoProgressService;

    // 从请求属性中获取当前用户ID
    private String getCurrentUserId(HttpServletRequest request) {
        return (String) request.getAttribute("userId");
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<VideoProgressDTO> getProgress(
            @PathVariable String resourceId,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        VideoProgress progress = videoProgressService.getOrCreateProgress(resourceId, userId);

        VideoProgressDTO dto = mapToVideoProgressDTO(progress);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{resourceId}/update")
    public ResponseEntity<VideoProgressDTO> updateProgress(
            @PathVariable String resourceId,
            @RequestBody UpdateProgressRequest request,
            HttpServletRequest httpRequest) {

        String userId = getCurrentUserId(httpRequest);

        VideoProgress progress = videoProgressService.updateProgress(
                resourceId,
                userId,
                request.getLastPosition(),
                request.getTotalWatched(),
                request.getSegments());

        VideoProgressDTO dto = mapToVideoProgressDTO(progress);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{resourceId}/report")
    public ResponseEntity<VideoProgressReportDTO> getProgressReport(
            @PathVariable String resourceId,
            HttpServletRequest request) {

        String userId = getCurrentUserId(request);

        VideoProgress progress = videoProgressService.getOrCreateProgress(resourceId, userId);
        VideoProgressReportDTO report = generateProgressReport(progress);

        return ResponseEntity.ok(report);
    }
    
    /**
     * 获取格式化的热力图数据
     */
    @GetMapping("/{resourceId}/heatmap")
    public ResponseEntity<HeatmapDataDTO> getHeatmapData(
            @PathVariable String resourceId,
            HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        HeatmapDataDTO heatmapData = videoProgressService.getFormattedHeatmapData(resourceId, userId);
        
        return ResponseEntity.ok(heatmapData);
    }

    /**
     * 获取时间轴热力图数据
     */
    @GetMapping("/{resourceId}/heatmap/timeline")
    public ResponseEntity<Map<String, Object>> getTimelineHeatmapData(
            @PathVariable String resourceId,
            @RequestParam(defaultValue = "10") int intervalSeconds,
            HttpServletRequest request) {
        
        String userId = getCurrentUserId(request);
        Map<String, Object> timelineData = videoProgressService.getTimelineHeatmapData(
                resourceId, userId, intervalSeconds);
        
        return ResponseEntity.ok(timelineData);
    }

    private VideoProgressDTO mapToVideoProgressDTO(VideoProgress progress) {
        return VideoProgressDTO.builder()
                .progressId(progress.getProgressId())
                .resourceId(progress.getVideo().getResourceId())
                .resourceName(progress.getVideo().getName())
                .lastPosition(progress.getLastPosition())
                .totalWatched(progress.getTotalWatched())
                .completionRate(progress.getCompletionRate())
                .heatmapData(progress.getHeatmapData())
                .lastWatchTime(progress.getLastWatchTime())
                .build();
    }

    private VideoProgressReportDTO generateProgressReport(VideoProgress progress) {
        // 这里可以添加更复杂的报告生成逻辑
        return VideoProgressReportDTO.builder()
                .resourceId(progress.getVideo().getResourceId())
                .resourceName(progress.getVideo().getName())
                .completionRate(progress.getCompletionRate())
                .totalWatchedMinutes(progress.getTotalWatched() / 60)
                .lastWatchTime(progress.getLastWatchTime())
                .heatmap(parseHeatmapData(progress.getHeatmapData()))
                .build();
    }

    private Map<String, Integer> parseHeatmapData(String heatmapData) {
        Map<String, Integer> heatmap = new HashMap<>();
        if (heatmapData == null || heatmapData.isEmpty()) {
            return heatmap;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(heatmapData);
            com.fasterxml.jackson.databind.JsonNode segments = root.get("segments");
            if (segments != null && segments.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode segment : segments) {
                    float start = segment.get("start").floatValue();
                    float end = segment.get("end").floatValue();
                    int count = segment.has("count") ? segment.get("count").asInt() : 1;
                    // 以秒为单位，统计每一秒的观看次数
                    for (int sec = (int) Math.floor(start); sec < (int) Math.ceil(end); sec++) {
                        String key = String.valueOf(sec);
                        heatmap.put(key, heatmap.getOrDefault(key, 0) + count);
                    }
                }
            }
        } catch (Exception e) {
            // 解析失败返回空map
        }
        return heatmap;
    }
}

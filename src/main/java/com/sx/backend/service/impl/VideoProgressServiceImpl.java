package com.sx.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.VideoSegment;
import com.sx.backend.dto.HeatmapDataDTO;
import com.sx.backend.dto.HeatmapSegmentDTO;
import com.sx.backend.dto.HeatmapStatsDTO;
import com.sx.backend.entity.Resource;
import com.sx.backend.entity.VideoProgress;
import com.sx.backend.exception.ResourceNotFoundException;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.mapper.VideoProgressMapper;
import com.sx.backend.service.VideoProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProgressServiceImpl implements VideoProgressService {
    private final VideoProgressMapper videoProgressMapper;
    private final ResourceMapper resourceMapper;
    private final ObjectMapper objectMapper;

    @Override
    public VideoProgress getOrCreateProgress(String resourceId, String userId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, userId);
        if (progress == null) {
            progress = createNewProgress(resourceId, userId);
        }
        return progress;
    }

    private VideoProgress createNewProgress(String resourceId, String userId) {
        Resource video = resourceMapper.getResourceById(resourceId);
        if (video == null) {
            throw new ResourceNotFoundException("视频资源不存在");
        }

        // 不再查找student表，兼容所有用户
        VideoProgress progress = new VideoProgress();
        progress.setProgressId(UUID.randomUUID().toString());
        progress.setVideo(video);
        progress.setLastPosition(0.0f);
        progress.setTotalWatched  (0.0f);
        progress.setCompletionRate(0.0f);
        progress.setHeatmapData(initializeHeatmapData(video.getDuration()));
        progress.setLastWatchTime(LocalDateTime.now());
        progress.setCreatedAt(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());
        progress.setUserId(userId);
        videoProgressMapper.insert(progress);
        return progress;
    }

    private String initializeHeatmapData(float duration) {
        try {
            Map<String, Object> heatmap = new HashMap<>();
            heatmap.put("version", 1);
            heatmap.put("duration", duration);
            heatmap.put("segments", new ArrayList<>());
            return objectMapper.writeValueAsString(heatmap);
        } catch (JsonProcessingException e) {
            log.error("初始化热力图数据失败", e);
            return "{\"version\":1,\"duration\":" + duration + ",\"segments\":[]}";
        }
    }

    @Override
    public VideoProgress updateProgress(String resourceId, String userId,
                                        Float lastPosition, Float totalWatched,
                                        List<VideoSegment> segments) {

        VideoProgress progress = getOrCreateProgress(resourceId, userId);
        Resource video = progress.getVideo();

        // 更新基础进度信息
        progress.setLastPosition(lastPosition);
        progress.setTotalWatched(totalWatched);
        float newCompletionRate = calculateCompletionRate(lastPosition, video.getDuration());
        // 只在新完成率更高时才更新
        if (newCompletionRate > progress.getCompletionRate()) {
            progress.setCompletionRate(newCompletionRate);
        }
        progress.setLastWatchTime(LocalDateTime.now());
        progress.setUpdatedAt(LocalDateTime.now());

        // 更新热力图数据
        updateHeatmapData(progress, segments);

        videoProgressMapper.update(progress);
        return progress;
    }

    private float calculateCompletionRate(float lastPosition, float duration) {
        if (duration <= 0) return 0;
        return Math.min(lastPosition / duration, 1.0f);
    }

    private void updateHeatmapData(VideoProgress progress, List<VideoSegment> newSegments) {
        if (newSegments == null || newSegments.isEmpty()) {
            return;
        }
        
        try {
            JsonNode heatmap = objectMapper.readTree(progress.getHeatmapData());
            ArrayNode segments = (ArrayNode) heatmap.get("segments");
            
            // 将现有片段转换为Map进行聚合
            Map<String, Integer> segmentMap = new HashMap<>();
            
            // 先处理现有片段
            if (segments != null) {
                for (JsonNode segment : segments) {
                    float start = segment.get("start").floatValue();
                    float end = segment.get("end").floatValue();
                    int count = segment.has("count") ? segment.get("count").asInt() : 1;
                    
                    addSegmentToMap(segmentMap, start, end, count);
                }
            }
            
            // 添加新片段
            for (VideoSegment segment : newSegments) {
                addSegmentToMap(segmentMap, segment.getStart(), segment.getEnd(), 
                               segment.getCount() != null ? segment.getCount() : 1);
            }
            
            // 重新构建segments数组
            ArrayNode newSegments_json = objectMapper.createArrayNode();
            for (Map.Entry<String, Integer> entry : segmentMap.entrySet()) {
                String[] range = entry.getKey().split("-");
                ObjectNode segmentNode = objectMapper.createObjectNode();
                segmentNode.put("start", Float.parseFloat(range[0]));
                segmentNode.put("end", Float.parseFloat(range[1]));
                segmentNode.put("count", entry.getValue());
                newSegments_json.add(segmentNode);
            }
            
            // 更新热力图数据
            ObjectNode newHeatmap = objectMapper.createObjectNode();
            newHeatmap.put("version", heatmap.get("version").asInt());
            newHeatmap.put("duration", heatmap.get("duration").asDouble());
            newHeatmap.set("segments", newSegments_json);
            
            progress.setHeatmapData(objectMapper.writeValueAsString(newHeatmap));
        } catch (JsonProcessingException e) {
            log.error("更新热力图数据失败", e);
        }
    }
    
    /**
     * 将片段添加到聚合Map中，相同区间会累加观看次数
     */
    private void addSegmentToMap(Map<String, Integer> segmentMap, float start, float end, int count) {
        // 将时间范围作为key，格式为 "start-end"
        String key = String.format("%.1f-%.1f", start, end);
        segmentMap.put(key, segmentMap.getOrDefault(key, 0) + count);
    }

    @Override
    public Optional<VideoProgress> getProgress(String resourceId, String userId) {
        return Optional.ofNullable(
                videoProgressMapper.selectByResourceAndStudent(resourceId, userId));
    }

    @Override
    public String getHeatmapData(String resourceId, String userId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, userId);
        return progress != null ? progress.getHeatmapData() : "{}";
    }

    @Override
    public Float calculateCompletionRate(String resourceId, String userId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, userId);
        return progress != null ? progress.getCompletionRate() : 0.0f;
    }

    @Override
    public List<VideoProgress> getCourseVideoProgress(String courseId, String userId) {
        return videoProgressMapper.selectByCourseAndStudent(courseId, userId);
    }

    /**
     * 获取格式化的热力图数据，专门用于前端渲染
     * @param resourceId 资源ID
     * @param userId 用户ID
     * @return 格式化的热力图数据
     */
    @Override
    public HeatmapDataDTO getFormattedHeatmapData(String resourceId, String userId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, userId);
        if (progress == null) {
            return createEmptyHeatmapData();
        }
        
        try {
            JsonNode heatmap = objectMapper.readTree(progress.getHeatmapData());
            
            // 基础信息
            double duration = heatmap.get("duration").asDouble();
            int version = heatmap.get("version").asInt();
            
            // 处理片段数据
            List<HeatmapSegmentDTO> segments = new ArrayList<>();
            JsonNode segmentsNode = heatmap.get("segments");
            
            int maxCount = 0;
            double totalWatchTime = 0.0;
            HeatmapSegmentDTO hottestSegment = null;
            
            if (segmentsNode != null && segmentsNode.isArray()) {
                for (JsonNode segment : segmentsNode) {
                    double start = segment.get("start").asDouble();
                    double end = segment.get("end").asDouble();
                    int count = segment.get("count").asInt();
                    
                    HeatmapSegmentDTO segmentDTO = HeatmapSegmentDTO.builder()
                            .start(start)
                            .end(end)
                            .count(count)
                            .intensity(calculateIntensity(count))
                            .duration(end - start)
                            .build();
                    
                    segments.add(segmentDTO);
                    
                    // 统计信息
                    maxCount = Math.max(maxCount, count);
                    totalWatchTime += (end - start) * count;
                    
                    if (hottestSegment == null || count > hottestSegment.getCount()) {
                        hottestSegment = segmentDTO;
                    }
                }
            }
            
            // 计算统计信息
            double averageCount = segments.isEmpty() ? 0.0 : 
                    segments.stream().mapToInt(HeatmapSegmentDTO::getCount).average().orElse(0.0);
            double coverageRate = duration > 0 ? 
                    segments.stream().mapToDouble(s -> s.getEnd() - s.getStart()).sum() / duration : 0.0;
            
            HeatmapStatsDTO stats = HeatmapStatsDTO.builder()
                    .totalWatchTime(totalWatchTime)
                    .averageWatchCount(averageCount)
                    .hottestSegmentStart(hottestSegment != null ? hottestSegment.getStart() : null)
                    .hottestSegmentEnd(hottestSegment != null ? hottestSegment.getEnd() : null)
                    .hottestSegmentCount(hottestSegment != null ? hottestSegment.getCount() : null)
                    .coverageRate(Math.min(coverageRate, 1.0))
                    .build();
            
            return HeatmapDataDTO.builder()
                    .duration(duration)
                    .version(version)
                    .segments(segments)
                    .totalSegments(segments.size())
                    .maxCount(maxCount)
                    .stats(stats)
                    .build();
                    
        } catch (JsonProcessingException e) {
            log.error("解析热力图数据失败", e);
            return createEmptyHeatmapData();
        }
    }
    
    /**
     * 创建空的热力图数据
     */
    private HeatmapDataDTO createEmptyHeatmapData() {
        HeatmapStatsDTO emptyStats = HeatmapStatsDTO.builder()
                .totalWatchTime(0.0)
                .averageWatchCount(0.0)
                .hottestSegmentStart(null)
                .hottestSegmentEnd(null)
                .hottestSegmentCount(null)
                .coverageRate(0.0)
                .build();
                
        return HeatmapDataDTO.builder()
                .duration(0.0)
                .version(1)
                .segments(new ArrayList<>())
                .totalSegments(0)
                .maxCount(0)
                .stats(emptyStats)
                .build();
    }
    
    /**
     * 计算片段的热度强度（0-1之间）
     */
    private double calculateIntensity(int count) {
        // 可以根据需要调整强度计算公式
        // 这里使用对数函数避免过大的数值
        return Math.min(1.0, Math.log(count + 1) / Math.log(10));
    }
    
    /**
     * 生成基于时间轴的热力图数据（按秒分割）
     * @param resourceId 资源ID  
     * @param userId 用户ID
     * @param intervalSeconds 时间间隔（秒）
     * @return 时间轴热力图数据
     */
    @Override
    public Map<String, Object> getTimelineHeatmapData(String resourceId, String userId, int intervalSeconds) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, userId);
        if (progress == null) {
            return createEmptyTimelineHeatmapData();
        }
        
        try {
            JsonNode heatmap = objectMapper.readTree(progress.getHeatmapData());
            double duration = heatmap.get("duration").asDouble();
            
            // 按时间间隔分割
            int totalIntervals = (int) Math.ceil(duration / intervalSeconds);
            int[] counts = new int[totalIntervals];
            
            JsonNode segmentsNode = heatmap.get("segments");
            if (segmentsNode != null && segmentsNode.isArray()) {
                for (JsonNode segment : segmentsNode) {
                    double start = segment.get("start").asDouble();
                    double end = segment.get("end").asDouble();
                    int count = segment.get("count").asInt();
                    
                    // 将片段的观看次数分配到相应的时间区间
                    int startInterval = (int) (start / intervalSeconds);
                    int endInterval = (int) (end / intervalSeconds);
                    
                    for (int i = startInterval; i <= endInterval && i < totalIntervals; i++) {
                        counts[i] += count;
                    }
                }
            }
            
            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("duration", duration);
            result.put("intervalSeconds", intervalSeconds);
            result.put("totalIntervals", totalIntervals);
            
            List<Map<String, Object>> intervals = new ArrayList<>();
            int maxCount = 0;
            
            for (int i = 0; i < totalIntervals; i++) {
                Map<String, Object> interval = new HashMap<>();
                interval.put("start", i * intervalSeconds);
                interval.put("end", Math.min((i + 1) * intervalSeconds, duration));
                interval.put("count", counts[i]);
                intervals.add(interval);
                maxCount = Math.max(maxCount, counts[i]);
            }
            
            // 计算每个区间的强度
            for (Map<String, Object> interval : intervals) {
                int count = (Integer) interval.get("count");
                double intensity = maxCount > 0 ? (double) count / maxCount : 0.0;
                interval.put("intensity", intensity);
            }
            
            result.put("intervals", intervals);
            result.put("maxCount", maxCount);
            
            return result;
        } catch (JsonProcessingException e) {
            log.error("生成时间轴热力图数据失败", e);
            return createEmptyTimelineHeatmapData();
        }
    }
    
    /**
     * 创建空的时间轴热力图数据
     */
    private Map<String, Object> createEmptyTimelineHeatmapData() {
        Map<String, Object> empty = new HashMap<>();
        empty.put("duration", 0.0);
        empty.put("intervalSeconds", 1);
        empty.put("totalIntervals", 0);
        empty.put("intervals", new ArrayList<>());
        empty.put("maxCount", 0);
        return empty;
    }
}
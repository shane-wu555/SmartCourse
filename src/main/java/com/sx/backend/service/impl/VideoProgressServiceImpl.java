package com.sx.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.VideoSegment;
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
    public VideoProgress getOrCreateProgress(String resourceId, String studentId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, studentId);
        if (progress == null) {
            progress = createNewProgress(resourceId, studentId);
        }
        return progress;
    }

    private VideoProgress createNewProgress(String resourceId, String studentId) {
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
        progress.setUserId(studentId);
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
    public VideoProgress updateProgress(String resourceId, String studentId,
                                        Float lastPosition, Float totalWatched,
                                        List<VideoSegment> segments) {

        VideoProgress progress = getOrCreateProgress(resourceId, studentId);
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
        try {
            JsonNode heatmap = objectMapper.readTree(progress.getHeatmapData());
            ArrayNode segments = (ArrayNode) heatmap.get("segments");

            for (VideoSegment segment : newSegments) {
                ObjectNode segmentNode = objectMapper.createObjectNode();
                segmentNode.put("start", segment.getStart());
                segmentNode.put("end", segment.getEnd());
                segmentNode.put("count", segment.getCount());
                segments.add(segmentNode);
            }

            progress.setHeatmapData(objectMapper.writeValueAsString(heatmap));
        } catch (JsonProcessingException e) {
            log.error("更新热力图数据失败", e);
        }
    }

    @Override
    public Optional<VideoProgress> getProgress(String resourceId, String studentId) {
        return Optional.ofNullable(
                videoProgressMapper.selectByResourceAndStudent(resourceId, studentId));
    }

    @Override
    public String getHeatmapData(String resourceId, String studentId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, studentId);
        return progress != null ? progress.getHeatmapData() : "{}";
    }

    @Override
    public Float calculateCompletionRate(String resourceId, String studentId) {
        VideoProgress progress = videoProgressMapper.selectByResourceAndStudent(resourceId, studentId);
        return progress != null ? progress.getCompletionRate() : 0.0f;
    }

    @Override
    public List<VideoProgress> getCourseVideoProgress(String courseId, String studentId) {
        return videoProgressMapper.selectByCourseAndStudent(courseId, studentId);
    }
}
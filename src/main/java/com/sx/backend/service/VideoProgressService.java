package com.sx.backend.service;

import com.sx.backend.dto.VideoSegment;
import com.sx.backend.dto.HeatmapDataDTO;
import com.sx.backend.entity.VideoProgress;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VideoProgressService {

    /**
     * 获取或创建视频学习进度记录
     */
    VideoProgress getOrCreateProgress(String resourceId, String userId);

    /**
     * 更新视频学习进度
     */
    VideoProgress updateProgress(String resourceId, String userId,
                                 Float lastPosition, Float totalWatched,
                                 List<VideoSegment> segments);

    /**
     * 获取视频学习进度
     */
    Optional<VideoProgress> getProgress(String resourceId, String userId);

    /**
     * 获取视频学习热力图数据
     */
    String getHeatmapData(String resourceId, String userId);

    /**
     * 获取格式化的热力图数据，专门用于前端渲染
     */
    HeatmapDataDTO getFormattedHeatmapData(String resourceId, String userId);

    /**
     * 生成基于时间轴的热力图数据（按秒分割）
     */
    Map<String, Object> getTimelineHeatmapData(String resourceId, String userId, int intervalSeconds);

    /**
     * 计算视频完成率
     */
    Float calculateCompletionRate(String resourceId, String userId);

    /**
     * 获取用户某课程的所有视频学习进度
     */
    List<VideoProgress> getCourseVideoProgress(String courseId, String userId);
}
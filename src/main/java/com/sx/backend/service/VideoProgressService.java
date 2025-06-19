package com.sx.backend.service;

import com.sx.backend.dto.VideoSegment;
import com.sx.backend.entity.VideoProgress;

import java.util.List;
import java.util.Optional;

public interface VideoProgressService {

    /**
     * 获取或创建视频学习进度记录
     */
    VideoProgress getOrCreateProgress(String resourceId, String studentId);

    /**
     * 更新视频学习进度
     */
    VideoProgress updateProgress(String resourceId, String studentId,
                                 Float lastPosition, Float totalWatched,
                                 List<VideoSegment> segments);

    /**
     * 获取视频学习进度
     */
    Optional<VideoProgress> getProgress(String resourceId, String studentId);

    /**
     * 获取视频学习热力图数据
     */
    String getHeatmapData(String resourceId, String studentId);

    /**
     * 计算视频完成率
     */
    Float calculateCompletionRate(String resourceId, String studentId);

    /**
     * 获取学生某课程的所有视频学习进度
     */
    List<VideoProgress> getCourseVideoProgress(String courseId, String studentId);
}
package com.sx.backend.mapper;

import com.sx.backend.entity.VideoProgress;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface VideoProgressMapper {

    // 插入新的视频进度记录
    int insert(VideoProgress progress);

    // 更新视频进度记录
    int update(VideoProgress progress);

    // 根据资源ID和学生ID查询
    VideoProgress selectByResourceAndStudent(
            @Param("resourceId") String resourceId,
            @Param("studentId") String studentId);

    // 查询学生某课程的所有视频进度
    List<VideoProgress> selectByCourseAndStudent(
            @Param("courseId") String courseId,
            @Param("studentId") String studentId);

    // 检查记录是否存在
    boolean exists(
            @Param("resourceId") String resourceId,
            @Param("studentId") String studentId);
}

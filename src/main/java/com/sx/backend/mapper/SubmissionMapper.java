package com.sx.backend.mapper;

import com.sx.backend.entity.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubmissionMapper {
    // 根据提交ID查询提交记录
    Submission findById(String submissionId);

    // 根据任务ID和学生ID查询提交记录
    Submission findByTaskIdAndStudentId(String taskId, String studentId);

    // 根据任务ID查询所有提交记录
    List<Submission> findByTaskId(String taskId);

    // 更新提交记录
    int update(Submission submission);

    // 创建新的提交记录
    int create(Submission submission);

    // 删除提交记录
    void delete(String submissionId);

    int insertFile(@Param("fileId") String fileId, @Param("submissionId") String submissionId, @Param("fileUrl") String fileUrl);

    int updateCompletedToTrue(@Param("submissionId") String submissionId);

    // 根据课程ID和学生ID查询提交记录
    List<Submission> findByCourseIdAndStudentId(String courseId, String studentId);
}

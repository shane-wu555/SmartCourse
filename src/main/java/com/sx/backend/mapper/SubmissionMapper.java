package com.sx.backend.mapper;

import com.sx.backend.entity.Submission;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SubmissionMapper {
    // 根据提交ID查询提交记录
    @Select(("SELECT * FROM submission WHERE submission_id = #{submissionId}"))
    Submission findById(String submissionId);

    // 根据任务ID和学生ID查询提交记录
    @Select("SELECT * FROM submission WHERE task_id = #{taskId} AND student_id = #{studentId}")
    Submission findByTaskIdAndStudentId(String taskId, String studentId);

    // 根据任务ID查询所有提交记录
    @Select("SELECT * FROM submission WHERE task_id = #{taskId}")
    List<Submission> findByTaskId(String taskId);

    // 更新提交记录
    @Update("UPDATE submission SET " +
            "submission_time = #{submissionTime}, " +
            "status = #{status}, " +
            "final_grade = #{finalGrade}, " +
            "auto_grade = #{autoGrade}, " +
            "feedback = #{feedback}, " +
            "completed = #{completed} " +
            "WHERE submission_id = #{submissionId}")
    int update(Submission submission);

    // 创建新的提交记录
    @Insert("INSERT INTO submission (submission_id, task_id, student_id, submission_time, status, final_grade, auto_grade, feedback, completed) " +
            "VALUES (#{submissionId}, #{taskId}, #{studentId}, #{submissionTime}, #{status}, #{finalGrade}, #{autoGrade}, #{feedback}, #{completed})")
    int create(Submission submission);

    // 删除提交记录
    @Delete("DELETE FROM submission WHERE submission_id = #{submissionId}")
    void delete(String submissionId);

    @Update("UPDATE submission SET completed = true WHERE submission_id = #{submissionId}")
    int updateCompletedToTrue(@Param("submissionId") String submissionId);
}

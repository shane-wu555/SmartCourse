package com.sx.backend.mapper;

import com.sx.backend.entity.TaskGrade;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TaskGradeMapper {
    /**
     * 根据成绩ID查询所有任务成绩
     * @return 任务成绩列表
     */
    @Select("SELECT * FROM task_grade WHERE student_id = #{studentId} AND course_id = #{courseId}")
    List<TaskGrade> findByStudentAndCourse(@Param("studentId") String studentId, @Param("courseId") String courseId);

    /**
     * 插入新任务成绩
     * @param taskGrade 任务成绩实体
     * @return 受影响的行数
     */
    @Insert("INSERT INTO task_grade (task_grade_id, student_id, task_id, score, feedback, graded_time, course_id) " +
            "VALUES (#{taskGradeId}, #{studentId}, #{taskId}, #{score}, #{feedback}, #{gradedTime}, #{courseId})" )
    int insert(TaskGrade taskGrade);

    /**
     * 更新任务成绩
     * @param taskGrade 任务成绩实体
     * @return 受影响的行数
     */
    @Update("UPDATE task_grade SET score = #{score}, feedback = #{feedback}, graded_time = #{gradedTime} WHERE task_id = #{taskId}")
    int update(TaskGrade taskGrade);

    /**
     * 根据学生和任务查询任务成绩
     * @param studentId 学生ID
     * @param taskId 任务ID
     * @return 任务成绩实体
     */
    @Select("SELECT * FROM task_grade WHERE student_id = #{studentId} AND task_id = #{taskId}")
    TaskGrade findByStudentAndTask(
            @Param("studentId") String studentId,
            @Param("taskId") String taskId);

    /**
     * 删除任务成绩
     * @param taskGradeId 任务成绩ID
     * @return 受影响的行数
     */
    @Delete("DELETE FROM task_grade WHERE id = #{taskGradeId}")
    int delete(String taskGradeId);
}

package com.sx.backend.mapper;

import com.sx.backend.entity.TaskGrade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskGradeMapper {
    /**
     * 根据成绩ID查询所有任务成绩
     * @param gradeId 成绩ID
     * @return 任务成绩列表
     */
    List<TaskGrade> findByGradeId(String gradeId);

    /**
     * 插入新任务成绩
     * @param taskGrade 任务成绩实体
     * @return 受影响的行数
     */
    int insert(TaskGrade taskGrade);

    /**
     * 更新任务成绩
     * @param taskGrade 任务成绩实体
     * @return 受影响的行数
     */
    int update(TaskGrade taskGrade);

    /**
     * 根据学生和任务查询任务成绩
     * @param studentId 学生ID
     * @param taskId 任务ID
     * @return 任务成绩实体
     */
    TaskGrade findByStudentAndTask(
            @Param("studentId") String studentId,
            @Param("taskId") String taskId);

    /**
     * 删除任务成绩
     * @param taskGradeId 任务成绩ID
     * @return 受影响的行数
     */
    int delete(String taskGradeId);
}

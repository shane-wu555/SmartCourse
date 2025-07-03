package com.sx.backend.mapper;

import com.sx.backend.entity.Grade;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface GradeMapper {
    /**
     * 根据学生ID和课程ID查询成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩实体
     */
    Grade findByStudentAndCourse(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId);

    /**
     * 根据课程ID查询所有成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    List<Grade> findByCourseId(String courseId);

    /**
     * 插入新成绩
     * @param grade 成绩实体
     * @return 受影响的行数
     */
    int insert(Grade grade);

    /**
     * 更新成绩
     * @param grade 成绩实体
     * @return 受影响的行数
     */
    int update(Grade grade);

    /**
     * 根据成绩ID查询成绩
     * @param gradeId 成绩ID
     * @return 成绩实体
     */
    Grade findById(String gradeId);

    /**
     * 根据学生ID查询所有成绩
     * @param studentId 学生ID
     * @return 成绩列表
     */
    List<Grade> findByStudentId(String studentId);
}

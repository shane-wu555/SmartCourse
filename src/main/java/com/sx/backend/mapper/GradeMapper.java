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
    @Select("SELECT * FROM grade WHERE student_id = #{studentId} AND course_id = #{courseId}")
    @Results({
            @Result(column = "grade_id", property = "gradeId"),
            @Result(column = "student_id", property = "studentId"),
            @Result(column = "course_id", property = "courseId"),
            @Result(column = "final_grade", property = "finalGrade"),
            @Result(column = "feedback", property = "feedback"),
            @Result(column = "grade_trend", property = "gradeTrend"),
            @Result(column = "rank_in_class", property = "rankInClass"),
            // 关联查询任务成绩
            @Result(property = "taskGrades", column = "student_id",
                    many = @Many(select = "selectTaskGradesByStudentAndCourse"))
    })
    Grade findByStudentAndCourse(
            @Param("studentId") String studentId,
            @Param("courseId") String courseId);

    /**
     * 根据课程ID查询所有成绩
     * @param courseId 课程ID
     * @return 成绩列表
     */
    @Select("SELECT * FROM grade WHERE course_id = #{courseId}")
    @Results({
        @Result(column = "grade_id", property = "gradeId"),
        @Result(column = "student_id", property = "studentId"),
        @Result(column = "course_id", property = "courseId"),
        @Result(column = "final_grade", property = "finalGrade"),
        @Result(column = "feedback", property = "feedback"),
        @Result(column = "grade_trend", property = "gradeTrend"),
        @Result(column = "rank_in_class", property = "rankInClass"),
        // 关联查询任务成绩
        @Result(property = "taskGrades", column = "student_id",
                many = @Many(select = "selectTaskGradesByStudentAndCourse"))
    })
    List<Grade> findByCourseId(String courseId);

    /**
     * 插入新成绩
     * @param grade 成绩实体
     * @return 受影响的行数
     */
    @Insert("INSERT INTO grade (id, student_id, course_id, score, grade_trend) VALUES (#{id}, #{studentId}, #{courseId}, #{score}, #{gradeTrend})")
    int insert(Grade grade);

    /**
     * 更新成绩
     * @param grade 成绩实体
     * @return 受影响的行数
     */
    @Update("UPDATE grade SET score = #{score}, grade_trend = #{gradeTrend} WHERE id = #{id}")
    int update(Grade grade);

    /**
     * 更新成绩趋势数据
     * @param gradeId 成绩ID
     * @param trendData 趋势数据
     * @return 受影响的行数
     */

    int updateGradeTrend(
            @Param("gradeId") String gradeId,
            @Param("trendData") Map<String, Object> trendData);

    /**
     * 根据成绩ID查询成绩
     * @param gradeId 成绩ID
     * @return 成绩实体
     */
    Grade findById(String gradeId);
}

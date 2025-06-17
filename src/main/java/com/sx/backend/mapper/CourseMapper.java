package com.sx.backend.mapper;

import com.sx.backend.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {
    // 获取教师课程列表
    List<Course> findByTeacherId(String teacherId);

    // 分页查询课程
    List<Course> findByTeacherIdWithPaging(
            @Param("teacherId") String teacherId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("semester") String semester,
            @Param("keyword") String keyword);

    int countByTeacherIdWithPaging(
            @Param("teacherId") String teacherId,
            @Param("semester") String semester,
            @Param("keyword") String keyword);

    // 创建课程
    int insert(Course course);

    // 获取课程详情
    Course findById(String courseId);

    // 更新课程
    int update(Course course);

    // 删除课程
    int delete(String courseId);

    // 检查课程编号唯一性
    int countByCourseCode(String courseCode);

    // 检查课程关联数据
    int countEnrollmentsByCourseId(String courseId);
    int countTasksByCourseId(String courseId);
}

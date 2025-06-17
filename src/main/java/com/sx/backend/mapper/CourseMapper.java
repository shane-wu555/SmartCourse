package com.sx.backend.mapper;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CourseMapper {
    // 返回DTO列表
    List<CourseDTO> findByTeacherId(String teacherId);

    List<CourseDTO> findByTeacherIdWithPaging(
            @Param("teacherId") String teacherId,
            @Param("offset") int offset,
            @Param("size") int size,
            @Param("semester") String semester,
            @Param("keyword") String keyword);

    int countByTeacherIdWithPaging(
            @Param("teacherId") String teacherId,
            @Param("semester") String semester,
            @Param("keyword") String keyword);

    int insert(Course course);

    // 返回DTO对象
    CourseDTO findById(String courseId);

    int update(Course course);
    int delete(String courseId);

    int countByCourseCode(
            @Param("courseCode") String courseCode,
            @Param("excludeCourseId") String excludeCourseId);

    int countEnrollmentsByCourseId(String courseId);
    int countTasksByCourseId(String courseId);
    int countResourcesByCourseId(String courseId);
}
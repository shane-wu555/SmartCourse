package com.sx.backend.service;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.entity.Course;
import com.sx.backend.service.impl.PageResult;

import java.util.List;

public interface CourseService {

    // 获取教师的所有课程列表
    List<CourseDTO> getCoursesByTeacherId(String teacherId);

    // 分页获取教师的课程列表
    PageResult<CourseDTO> getCoursesByPage(
            String teacherId,
            int page,
            int size,
            String semester,
            String keyword);

    // 创建新课程
    CourseDTO createCourse(CourseCreateRequest request, String teacherId);

    // 获取课程详情
    CourseDTO getCourseDetail(String courseId, String teacherId);

    // 更新课程信息
    CourseDTO updateCourse(
            String courseId,
            CourseUpdateRequest request,
            String teacherId);

    // 删除课程
    void deleteCourse(String courseId, String teacherId);

    String getCurrentTeacherId();

    Course getCourseEntityById(String courseId);

    CourseDTO getStudentCourseDetail(String courseId);
}
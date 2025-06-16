package com.sx.backend.service;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.service.impl.PageResult;

import java.util.List;

public interface CourseService {
    List<CourseDTO> getCoursesByTeacherId(String teacherId);
    PageResult<CourseDTO> getCoursesByPage(String teacherId, int page, int size, String semester, String keyword);
    CourseDTO createCourse(CourseCreateRequest request, String teacherId);
    CourseDTO getCourseDetail(String courseId, String teacherId);
    CourseDTO updateCourse(String courseId, CourseUpdateRequest request, String teacherId);
    void deleteCourse(String courseId, String teacherId);
}


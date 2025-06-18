package com.sx.backend.service;

import com.sx.backend.entity.Course;
import com.sx.backend.service.impl.PageResult;

import java.util.List;

public interface StudentService {
    List<Course> getAllCourses(String studentId);
    PageResult<Course> getCoursesByPage(String studentId, int page, int size);
    boolean enrollCourse(String studentId, String courseId);
    boolean dropCourse(String studentId, String courseId);
    PageResult<Course> searchCourses(String studentId, String keyword, int page, int size);
    Course getCourseDetail(String studentId, String courseId);
}

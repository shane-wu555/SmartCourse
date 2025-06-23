package com.sx.backend.controller;

import com.sx.backend.entity.Course;
import com.sx.backend.service.StudentService;
import com.sx.backend.service.impl.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/courses")
public class StudentCourseController {

    private final StudentService studentService;

    @Autowired
    public StudentCourseController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 从请求属性中获取当前用户ID
    private String getCurrentStudentId(HttpServletRequest request) {
        return (String) request.getAttribute("userId");
    }

    // 获取学生已选课程（分页）
    @GetMapping
    public ResponseEntity<PageResult<Course>> getEnrolledCourses(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String studentId = getCurrentStudentId(request);
        PageResult<Course> result = studentService.getCoursesByPage(studentId, page, size);
        return ResponseEntity.ok(result);
    }

    // 搜索可选课程
    @GetMapping("/search")
    public ResponseEntity<PageResult<Course>> searchCourses(
            HttpServletRequest request,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        String studentId = getCurrentStudentId(request);
        PageResult<Course> result = studentService.searchCourses(studentId, keyword, page, size);
        return ResponseEntity.ok(result);
    }

    // 选课操作
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<?> enrollCourse(
            HttpServletRequest request,
            @PathVariable String courseId) {
        String studentId = getCurrentStudentId(request);
        studentService.enrollCourse(studentId, courseId);
        return ResponseEntity.ok().build();
    }

    // 退课操作
    @PostMapping("/{courseId}/drop")
    public ResponseEntity<?> dropCourse(
            HttpServletRequest request,
            @PathVariable String courseId) {
        String studentId = getCurrentStudentId(request);
        studentService.dropCourse(studentId, courseId);
        return ResponseEntity.ok().build();
    }

    // 获取课程详情（验证学生权限）
    @GetMapping("/{courseId}")
    public ResponseEntity<Course> getCourseDetail(
            HttpServletRequest request,
            @PathVariable String courseId) {
        String studentId = getCurrentStudentId(request);
        Course course = studentService.getCourseDetail(studentId, courseId);
        return ResponseEntity.ok(course);
    }
}
package com.sx.backend.controller;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.Course;
import com.sx.backend.service.CourseService;
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
    private final CourseService courseService;

    @Autowired
    public StudentCourseController(StudentService studentService, CourseService courseService) {
        this.studentService = studentService;
        this.courseService = courseService;
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
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseDetail(
            HttpServletRequest request,
            @PathVariable String courseId) {
        String studentId = getCurrentStudentId(request);
        // 验证学生是否选修了该课程
        studentService.getCourseDetail(studentId, courseId);
        // 返回完整的课程详情
        CourseDTO courseDTO = courseService.getStudentCourseDetail(courseId);
        ApiResponse<CourseDTO> response = ApiResponse.success("成功获取课程详情", courseDTO);
        return ResponseEntity.ok(response);
    }
}
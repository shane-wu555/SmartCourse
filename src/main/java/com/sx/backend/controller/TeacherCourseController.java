package com.sx.backend.controller;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.service.CourseService;
import com.sx.backend.service.impl.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;
    private final HttpServletRequest request; // 注入HttpServletRequest

    private String getCurrentTeacherId() {
        // 从请求属性中获取拦截器设置的userId
        return (String) request.getAttribute("userId");
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getTeacherCourses() {
        String teacherId = getCurrentTeacherId();
        List<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId);
        ApiResponse<List<CourseDTO>> response = ApiResponse.success("成功获取课程列表", courses);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.createCourse(request, teacherId);
        ApiResponse<CourseDTO> response = ApiResponse.success(201, "课程创建成功", courseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<PageResult<CourseDTO>>> getCoursesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String keyword) {

        size = Math.min(size, 100);
        String teacherId = getCurrentTeacherId();
        PageResult<CourseDTO> result = courseService.getCoursesByPage(
                teacherId, page, size, semester, keyword);
        ApiResponse<PageResult<CourseDTO>> response = ApiResponse.success("成功获取课程列表", result);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseDetail(@PathVariable String courseId) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.getCourseDetail(courseId, teacherId);
        ApiResponse<CourseDTO> response = ApiResponse.success("成功获取课程详情", courseDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.updateCourse(courseId, request, teacherId);
        ApiResponse<CourseDTO> response = ApiResponse.success("课程更新成功", courseDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable String courseId) {
        String teacherId = getCurrentTeacherId();
        courseService.deleteCourse(courseId, teacherId);
        return ResponseEntity.noContent().build();
    }
}
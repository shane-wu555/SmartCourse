package com.sx.backend.controller;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.request.CourseCreateRequest;
import com.sx.backend.dto.request.CourseUpdateRequest;
import com.sx.backend.service.CourseService;
import com.sx.backend.service.impl.PageResult;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/courses")
@RequiredArgsConstructor
public class TeacherCourseController {

    private final CourseService courseService;

    // 获取当前教师ID（模拟实现，实际应从SecurityContext获取）
    private String getCurrentTeacherId() {
        // TODO: 实现从安全上下文获取真实教师ID
        return "t001";
    }

    // 1. 获取教师课程列表
    @GetMapping
    public ApiResponse<List<CourseDTO>> getTeacherCourses() {
        String teacherId = getCurrentTeacherId();
        List<CourseDTO> courses = courseService.getCoursesByTeacherId(teacherId);
        return ApiResponse.success(courses);
    }

    // 2. 创建新课程
    @PostMapping
    public ApiResponse<CourseDTO> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.createCourse(request, teacherId);
        return ApiResponse.success(201, "课程创建成功", courseDTO);
    }

    // 6. 分页查询课程
    /*@GetMapping("/page")
    public ApiResponse<PageResult<CourseDTO>> getCoursesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String keyword) {

        // 限制每页最大数量
        size = Math.min(size, 100);

        String teacherId = getCurrentTeacherId();
        PageResult<CourseDTO> result = courseService.getCoursesByPage(
                teacherId, page, size, semester, keyword);
        return ApiResponse.success(result);
    }*/

    // 3. 获取课程详情
    @GetMapping("/{courseId}")
    public ApiResponse<CourseDTO> getCourseDetail(@PathVariable String courseId) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.getCourseDetail(courseId, teacherId);
        return ApiResponse.success(courseDTO);
    }

    // 4. 更新课程信息
    @PutMapping("/{courseId}")
    public ApiResponse<CourseDTO> updateCourse(
            @PathVariable String courseId,
            @Valid @RequestBody CourseUpdateRequest request) {
        String teacherId = getCurrentTeacherId();
        CourseDTO courseDTO = courseService.updateCourse(courseId, request, teacherId);
        return ApiResponse.success("课程更新成功", courseDTO);
    }

    // 5. 删除课程
    @DeleteMapping("/{courseId}")
    public ApiResponse<Void> deleteCourse(@PathVariable String courseId) {
        String teacherId = getCurrentTeacherId();
        courseService.deleteCourse(courseId, teacherId);
        return ApiResponse.success(204, "课程删除成功", null);
    }


}


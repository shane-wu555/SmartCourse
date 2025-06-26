package com.sx.backend.controller;

import com.sx.backend.dto.TeacherStudentDTO;
import com.sx.backend.service.TeacherStudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher")
public class TeacherStudentController {

    private final TeacherStudentService teacherStudentService;
    private final HttpServletRequest request;

    public TeacherStudentController(TeacherStudentService teacherStudentService,
                                    HttpServletRequest request) {
        this.teacherStudentService = teacherStudentService;
        this.request = request;
    }

    // 从请求属性中获取当前用户ID（教师ID）
    private String getCurrentTeacherId() {
        return (String) request.getAttribute("userId");
    }

    // 获取课程学生列表
    @GetMapping("/courses/{courseId}/students")
    public ResponseEntity<List<TeacherStudentDTO>> getCourseStudents(
            @PathVariable String courseId) {
        String teacherId = getCurrentTeacherId();
        List<TeacherStudentDTO> students =
                teacherStudentService.getStudentsByCourseId(courseId, teacherId);
        return ResponseEntity.ok(students);
    }

    // 批量导入学生到课程
    @PostMapping("/courses/{courseId}/import-students")
    public ResponseEntity<Map<String, Object>> importStudents(
            @PathVariable String courseId,
            @RequestParam("file") MultipartFile file) {
        String teacherId = getCurrentTeacherId();
        Map<String, Object> result =
                teacherStudentService.importStudentsToCourse(courseId, teacherId, file);
        return ResponseEntity.ok(result);
    }
}
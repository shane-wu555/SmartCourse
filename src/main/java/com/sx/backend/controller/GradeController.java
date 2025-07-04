package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.GradeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeMapper gradeMapper;

    private final HttpServletRequest request;

    private String getCurrentStudentId() {
        // 从请求属性中获取拦截器设置的userId
        return (String) request.getAttribute("userId");
    }

    // 获取学生的所有课程成绩
    @GetMapping
    public ResponseEntity<List<Grade>> getGradesByStudentId() {
        String studentId = getCurrentStudentId();
        List<Grade> grades = gradeMapper.findByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }

    // 获取课程的所有学生成绩
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Grade>> getGradesByCourseId(@PathVariable String courseId) {
        List<Grade> grades = gradeMapper.findByCourseId(courseId);
        return ResponseEntity.ok(grades);
    }

    // 获取学生在特定课程的成绩
    @GetMapping("/course/{courseId}/student")
    public ResponseEntity<Grade> getGrade(
            @PathVariable String courseId) {
        String studentId = getCurrentStudentId();
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);

        // 处理没有成绩的情况
        if (grade == null) {
            grade = new Grade();
            grade.setStudentId(studentId);
            grade.setCourseId(courseId);
            grade.setTaskGrades(Collections.emptyList());
        }

        return ResponseEntity.ok(grade);
    }


}

package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeMapper gradeMapper;

    // 获取学生的所有课程成绩
    @GetMapping("/{studentId}")
    public ResponseEntity<List<Grade>> getGradesByStudentId(@PathVariable String studentId) {
        List<Grade> grades = gradeMapper.findByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }

    // 获取课程的所有学生成绩
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Grade>> getGradesByCourseId(@PathVariable String courseId) {
        List<Grade> grades = gradeMapper.findByCourseId(courseId);
        return ResponseEntity.ok(grades);
    }

    // 更新学生的课程成绩
    @PostMapping("/taskGrade")
    public ResponseEntity<Void> updateTaskGrade(@RequestBody TaskGrade taskGrade) {
        gradeService.updateTaskGrade(taskGrade);
        return ResponseEntity.ok().build();
    }

    // 获取学生在特定课程的成绩
    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<Grade> getGrade(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(grade);
    }
}

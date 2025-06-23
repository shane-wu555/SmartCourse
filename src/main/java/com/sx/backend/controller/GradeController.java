package com.sx.backend.controller;

import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/grades")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private GradeMapper gradeMapper;

    @PostMapping("/task")
    public ResponseEntity<Void> updateTaskGrade(@RequestBody TaskGrade taskGrade) {
        gradeService.updateTaskGrade(taskGrade);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{studentId}/{courseId}")
    public ResponseEntity<Grade> getGrade(
            @PathVariable String studentId,
            @PathVariable String courseId) {

        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        return ResponseEntity.ok(grade);
    }
}

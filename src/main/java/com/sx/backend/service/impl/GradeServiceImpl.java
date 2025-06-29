package com.sx.backend.service.impl;

import com.sx.backend.entity.Course;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.AnalysisService;
import com.sx.backend.service.CourseService;
import com.sx.backend.service.GradeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GradeServiceImpl implements GradeService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private CourseService courseService; // 注入CourseService

    @Override
    @Transactional
    public void updateTaskGrade(TaskGrade taskGrade) {
        // 保存或更新任务成绩
        if (taskGrade.getTaskGradeId() == null) {
            taskGrade.setTaskGradeId(UUID.randomUUID().toString());
            taskGradeMapper.insert(taskGrade);
        } else {
            taskGradeMapper.update(taskGrade);
        }

        // 更新关联的总成绩
        updateFinalGrade(taskGrade);

        // 更新成绩趋势
        analysisService.updateGradeTrend(taskMapper.getById(taskGrade.getTaskId()).getCourseId(),
                                         taskGrade.getStudentId());
    }

    public void updateFinalGrade(TaskGrade taskGrade) {

        Grade grade = gradeMapper.findByStudentAndCourse(taskMapper.getById(taskGrade.getTaskId()).getCourseId(),
                                                         taskGrade.getStudentId());

        if (grade == null) {
            grade = new Grade();
            grade.setGradeId(UUID.randomUUID().toString());
            grade.setStudentId(taskGrade.getStudentId());
            grade.setCourseId(taskMapper.getById(taskGrade.getTaskId()).getCourseId());
            grade.setFinalGrade(0.0f);
            gradeMapper.insert(grade);
        }

        // 重新计算最终成绩
        List<TaskGrade> taskGrades = taskGradeMapper.findByGradeId(grade.getGradeId());
        float totalScore = 0;

        for (TaskGrade tg : taskGrades) {
            totalScore += tg.getScore();
        }

        grade.setFinalGrade(totalScore);

        gradeMapper.update(grade);
    }
}

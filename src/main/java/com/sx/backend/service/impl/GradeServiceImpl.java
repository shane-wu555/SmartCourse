package com.sx.backend.service.impl;

import com.sx.backend.entity.Course;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
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
        analysisService.updateGradeTrend(taskGrade.getTask().getCourseId(),
                                         taskGrade.getStudent().getStudentNumber());
    }

    public void updateFinalGrade(TaskGrade taskGrade) {
        Grade grade = gradeMapper.findByStudentAndCourse(taskGrade.getTask().getCourseId(),
                                                         taskGrade.getStudent().getStudentNumber());

        if (grade == null) {
            grade = new Grade();
            grade.setGradeId(UUID.randomUUID().toString());
            grade.setStudent(taskGrade.getStudent());
            // 使用注入的courseService实例获取课程实体
            Course course = courseService.getCourseEntityById(
                    taskGrade.getTask().getCourseId()
            );
            grade.setCourse(course);
            grade.setFinalGrade(0.0f);
            gradeMapper.insert(grade);
        }

        // 重新计算最终成绩（加权平均）
        List<TaskGrade> taskGrades = taskGradeMapper.findByGradeId(grade.getGradeId());
        float totalScore = 0;

        for (TaskGrade tg : taskGrades) {
            totalScore += tg.getScore();
        }

        grade.setFinalGrade(totalScore);

        gradeMapper.update(grade);
    }
}

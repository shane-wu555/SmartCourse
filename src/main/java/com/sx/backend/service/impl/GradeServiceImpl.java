package com.sx.backend.service.impl;

import com.sx.backend.entity.Course;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Submission;
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

import java.time.LocalDateTime;
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

    @Override
    @Transactional
    public void updateTaskGrade(Submission submission) {
        String taskId = submission.getTaskId();
        String studentId = submission.getStudentId();
        TaskGrade taskGrade;
        if (taskGradeMapper.findByStudentAndTask(studentId, taskId) == null) {
            // 如果没有找到对应的TaskGrade，创建一个新的
            taskGrade = new TaskGrade();
            taskGrade.setTaskId(taskId);
            taskGrade.setStudentId(studentId);
            taskGrade.setScore(submission.getFinalGrade());
            taskGrade.setGradedTime(LocalDateTime.now());
            taskGrade.setCourseId(taskMapper.getById(taskId).getCourseId());
            taskGrade.setTaskGradeId(UUID.randomUUID().toString());
            taskGradeMapper.insert(taskGrade);
        }
        else {
            // 如果已经存在，更新成绩
            taskGrade = taskGradeMapper.findByStudentAndTask(studentId, taskId);
            taskGrade.setScore(submission.getFinalGrade());
            taskGrade.setFeedback(submission.getFeedback());
            taskGrade.setGradedTime(LocalDateTime.now());
            taskGradeMapper.update(taskGrade);
        }

        // 更新关联的总成绩
        updateFinalGrade(taskGrade);

        // 更新成绩趋势
        analysisService.updateGradeTrend(taskGrade.getCourseId(),
                                         taskGrade.getStudentId());
    }

    // 更新最终成绩
    public void updateFinalGrade(TaskGrade taskGrade) {

        Grade grade = gradeMapper.findByStudentAndCourse(taskGrade.getStudentId(), taskGrade.getCourseId());

        if (grade == null) {
            grade = new Grade();
            grade.setGradeId(UUID.randomUUID().toString());
            grade.setStudentId(taskGrade.getStudentId());
            grade.setCourseId(taskGrade.getCourseId());
            grade.setFinalGrade(0.0f);
            gradeMapper.insert(grade);
        }

        // 重新计算最终成绩
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(grade.getStudentId(), grade.getCourseId());
        float totalScore = 0;

        for (TaskGrade tg : taskGrades) {
            totalScore += tg.getScore();
        }

        grade.setFinalGrade(totalScore);
        gradeMapper.update(grade);

        // 更新排名
        List<Grade> allGrades = gradeMapper.findByCourseId(grade.getCourseId());
        allGrades.sort((g1, g2) -> Float.compare(g2.getFinalGrade(), g1.getFinalGrade()));
        int rank = 1;
        for (Grade g : allGrades) {
            g.setRankInClass(rank++);
            gradeMapper.update(g);
        }

        analysisService.updateGradeTrend(grade.getStudentId(), grade.getCourseId());
    }
}

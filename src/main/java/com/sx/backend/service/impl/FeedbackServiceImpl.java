package com.sx.backend.service.impl;

import com.sx.backend.entity.*;
import com.sx.backend.mapper.*;
import com.sx.backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public void generateFeedback(String studentId, String courseId) {
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) {
            throw new IllegalArgumentException("Grade not found for student: " + studentId + " in course: " + courseId);
        }

        // 生成反馈消息
        StringBuilder message = new StringBuilder();
        message.append("你的当前成绩为: ").append(grade.getFinalGrade()).append("\n");
        message.append("班级排名: ").append(grade.getRankInClass()).append("\n\n");
        float totalScore = taskMapper.findTotalScoreByCourseId(courseId);
        float scoreRate = (grade.getFinalGrade() / totalScore) * 100;

        if (scoreRate >= 90) {
            message.append("你已经掌握基础内容，可以尝试拓展学习。");
        } else if (scoreRate >= 75) {
            message.append("部分知识点需要巩固，建议重点复习错题。");
        } else {
            message.append("你的基础知识点掌握不够牢固，建议复习基础内容。");
        }

        // 添加具体建议
        List<TaskGrade> taskGrades = taskGradeMapper.findByStudentAndCourse(grade.getStudentId(), grade.getCourseId());
        if (taskGrades.isEmpty()) {
            throw new IllegalArgumentException("未找到任何任务成绩记录");
        }
        taskGrades.sort(Comparator.comparing(TaskGrade::getScore));
        TaskGrade minScoreTask = taskGrades.get(0) != null ? taskGrades.get(0): null;
        if (minScoreTask != null) {
            float minScore = (minScoreTask.getScore() / taskMapper.getById(minScoreTask.getTaskId()).getMaxScore()) * 100;

            if (minScore < 60) {
                message.append("\n\n建议重点关注以下任务:");
                for (TaskGrade taskGrade : taskGrades) {
                    if (((taskGrade.getScore() / taskMapper.getById(taskGrade.getTaskId()).getMaxScore()) * 100)< 60) {
                        message.append("\n- ").append(taskMapper.getById(taskGrade.getTaskId()).getTitle()).append("\n");
                    }
                }
            }
        }


        grade.setFeedback(message.toString());

        gradeMapper.update(grade);
    }
}
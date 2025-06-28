package com.sx.backend.service.impl;

import com.sx.backend.dto.FeedbackDTO;
import com.sx.backend.entity.*;
import com.sx.backend.mapper.*;
import com.sx.backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private GradeMapper gradeMapper;

    @Autowired
    private TaskGradeMapper taskGradeMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Override
    public FeedbackDTO generateFeedback(String studentId, String courseId) {
        Grade grade = gradeMapper.findByStudentAndCourse(studentId, courseId);
        if (grade == null) return null;

        FeedbackDTO feedback = new FeedbackDTO();
        feedback.setStudentId(studentId);
        feedback.setCourseId(courseId);
        feedback.setFinalGrade(grade.getFinalGrade());
        feedback.setRankInClass(grade.getRankInClass());

        // 生成反馈消息
        StringBuilder message = new StringBuilder();
        message.append("你的当前成绩为: ").append(grade.getFinalGrade()).append("\n");
        message.append("班级排名: ").append(grade.getRankInClass()).append("\n\n");

        if (grade.getFinalGrade() >= 90) {
            message.append("你已经掌握基础内容，可以尝试拓展学习。");
        } else if (grade.getFinalGrade() >= 75) {
            message.append("部分知识点需要巩固，建议重点复习错题。");
        } else {
            message.append("你的基础知识点掌握不够牢固，建议复习基础内容。");
        }

        // 添加具体建议
        List<TaskGrade> taskGrades = taskGradeMapper.findByGradeId(grade.getGradeId());
        double minScore = taskGrades.stream()
                .mapToDouble(TaskGrade::getScore)
                .min()
                .orElse(0);

        if (minScore < 60) {
            message.append("\n\n建议重点关注以下任务:");
            for (TaskGrade taskGrade : taskGrades) {
                if (taskGrade.getScore() < 60) {
                    message.append("\n- ").append(taskMapper.getById(taskGrade.getTaskId()).getTitle()).append("\n");
                }
            }
        }

        feedback.setMessage(message.toString());
        return feedback;
    }
}
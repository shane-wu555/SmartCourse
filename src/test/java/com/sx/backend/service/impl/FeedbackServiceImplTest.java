package com.sx.backend.service.impl;

import com.sx.backend.dto.FeedbackDTO;
import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    // 测试数据准备
    private Grade createGrade(String gradeId, float finalGrade, int rank) {
        Grade grade = new Grade();
        grade.setGradeId(gradeId);
        grade.setFinalGrade(finalGrade);
        grade.setRankInClass(rank);
        return grade;
    }

    private TaskGrade createTaskGrade(String taskId, float score) {
        TaskGrade taskGrade = new TaskGrade();
        taskGrade.setTaskId(taskId);
        taskGrade.setScore(score);
        return taskGrade;
    }

    private Task createTask(String taskId, String title) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setTitle(title);
        return task;
    }

    @Test
    void generateFeedback_ShouldReturnNull_WhenNoGradeFound() {
        // 准备
        String studentId = "s001";
        String courseId = "c001";
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null);

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNull(result);
        verify(gradeMapper).findByStudentAndCourse(studentId, courseId);
        verifyNoInteractions(taskGradeMapper, taskMapper);
    }

    @Test
    void generateFeedback_ShouldGenerateExcellentFeedback() {
        // 准备
        String studentId = "s001";
        String courseId = "c001";
        Grade grade = createGrade("g001", 95, 1);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g001")).thenReturn(Collections.emptyList());

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);
        assertEquals(studentId, result.getStudentId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(95.0, result.getFinalGrade());
        assertEquals(1, result.getRankInClass());

        String expectedMessage = "你的当前成绩为: 95.0\n" +
                "班级排名: 1\n" +
                "\n" +
                "你已经掌握基础内容，可以尝试拓展学习。";
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    void generateFeedback_ShouldGenerateGoodFeedback() {
        // 准备
        String studentId = "s002";
        String courseId = "c002";
        Grade grade = createGrade("g002", 80, 10);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g002")).thenReturn(Collections.emptyList());

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);

        String expectedMessage = "你的当前成绩为: 80.0\n" +
                "班级排名: 10\n" +
                "\n" +
                "部分知识点需要巩固，建议重点复习错题。";
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    void generateFeedback_ShouldGeneratePoorFeedback() {
        // 准备
        String studentId = "s003";
        String courseId = "c003";
        Grade grade = createGrade("g003", 65, 25);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g003")).thenReturn(Collections.emptyList());

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);

        String expectedMessage = "你的当前成绩为: 65.0\n" +
                "班级排名: 25\n" +
                "\n" +
                "你的基础知识点掌握不够牢固，建议复习基础内容。";
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    void generateFeedback_ShouldIncludeTaskRecommendations() {
        // 准备
        String studentId = "s004";
        String courseId = "c004";
        Grade grade = createGrade("g004", 70, 20);

        // 创建任务成绩
        TaskGrade lowTask1 = createTaskGrade("t001", 55);
        TaskGrade lowTask2 = createTaskGrade("t002", 40);
        TaskGrade passTask = createTaskGrade("t003", 75);
        List<TaskGrade> taskGrades = Arrays.asList(lowTask1, lowTask2, passTask);

        // 创建任务
        Task task1 = createTask("t001", "基础语法测验");
        Task task2 = createTask("t002", "面向对象作业");

        // 设置模拟行为
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g004")).thenReturn(taskGrades);
        when(taskMapper.getById("t001")).thenReturn(task1);
        when(taskMapper.getById("t002")).thenReturn(task2);
        when(taskMapper.getById("t003")).thenReturn(null); // 测试空任务处理

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);

        String expectedMessage = "你的当前成绩为: 70.0\n" +
                "班级排名: 20\n" +
                "\n" +
                "你的基础知识点掌握不够牢固，建议复习基础内容。" +
                "\n\n建议重点关注以下任务:\n" +
                "- 基础语法测验\n" +
                "- 面向对象作业\n";
        assertEquals(expectedMessage, result.getMessage());
    }

    @Test
    void generateFeedback_ShouldHandleNullTasksGracefully() {
        // 准备
        String studentId = "s005";
        String courseId = "c005";
        Grade grade = createGrade("g005", 50, 30);

        // 创建任务成绩（但任务不存在）
        TaskGrade lowTask = createTaskGrade("t004", 30);
        List<TaskGrade> taskGrades = Collections.singletonList(lowTask);

        // 设置模拟行为
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g005")).thenReturn(taskGrades);
        when(taskMapper.getById("t004")).thenReturn(null); // 任务不存在

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);

        // 应包含建议部分但任务名为空
        assertTrue(result.getMessage().contains("建议重点关注以下任务:"));
        assertTrue(result.getMessage().contains("- null\n")); // 处理了空任务
    }

    @Test
    void generateFeedback_ShouldNotIncludeTasks_WhenAllPass() {
        // 准备
        String studentId = "s006";
        String courseId = "c006";
        Grade grade = createGrade("g006", 85, 5);

        // 所有任务成绩都及格
        TaskGrade task1 = createTaskGrade("t005", 80);
        TaskGrade task2 = createTaskGrade("t006", 90);
        List<TaskGrade> taskGrades = Arrays.asList(task1, task2);

        // 设置模拟行为
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g006")).thenReturn(taskGrades);

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);

        // 验证消息不包含任务建议部分
        assertFalse(result.getMessage().contains("建议重点关注以下任务:"));
        assertFalse(result.getMessage().contains("基础语法测验"));
    }

    @Test
    void generateFeedback_ShouldHandleEdgeCaseGrades() {
        // 边界值测试：刚好90分
        String studentId = "s007";
        String courseId = "c007";
        Grade grade = createGrade("g007", 90, 3);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g007")).thenReturn(Collections.emptyList());

        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);
        assertTrue(result.getMessage().contains("你已经掌握基础内容"));

        // 边界值测试：刚好75分
        studentId = "s008";
        courseId = "c008";
        grade = createGrade("g008", 75, 15);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g008")).thenReturn(Collections.emptyList());

        result = feedbackService.generateFeedback(studentId, courseId);
        assertTrue(result.getMessage().contains("部分知识点需要巩固"));

        // 边界值测试：刚好74.9分
        studentId = "s009";
        courseId = "c009";
        grade = createGrade("g009", 74.9F, 20);
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g009")).thenReturn(Collections.emptyList());

        result = feedbackService.generateFeedback(studentId, courseId);
        assertTrue(result.getMessage().contains("基础知识点掌握不够牢固"));
    }

    @Test
    void generateFeedback_ShouldHandleNoTaskGrades() {
        // 准备
        String studentId = "s010";
        String courseId = "c010";
        Grade grade = createGrade("g010", 85, 8);

        // 没有任务成绩
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade);
        when(taskGradeMapper.findByGradeId("g010")).thenReturn(Collections.emptyList());

        // 执行
        FeedbackDTO result = feedbackService.generateFeedback(studentId, courseId);

        // 验证
        assertNotNull(result);
        assertFalse(result.getMessage().contains("建议重点关注以下任务"));
    }
}

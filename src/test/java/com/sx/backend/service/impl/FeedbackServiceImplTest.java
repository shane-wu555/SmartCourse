package com.sx.backend.service.impl;

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
class FeedbackServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    // 测试成绩不存在的情况
    @Test
    void generateFeedback_WhenGradeNotFound_ThrowsException() {
        when(gradeMapper.findByStudentAndCourse("s001", "c001")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> feedbackService.generateFeedback("s001", "c001")
        );
        assertEquals("Grade not found for student: s001 in course: c001", exception.getMessage());
        verify(gradeMapper, never()).update(any());
    }

    // 测试90分以上的情况
    @Test
    void generateFeedback_ScoreAbove90_GeneratesCorrectFeedback() {
        Grade grade = createTestGrade("s001", "c001", 95.0f, 5);
        List<TaskGrade> taskGrades = createPassingTaskGrades();

        when(gradeMapper.findByStudentAndCourse("s001", "c001")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c001")).thenReturn(100.0f);
        when(taskGradeMapper.findByStudentAndCourse("s001", "c001")).thenReturn(taskGrades);
        when(taskMapper.getById(anyString())).thenReturn(createTask(100.0f));

        feedbackService.generateFeedback("s001", "c001");

        String expectedFeedback =
                "你的当前成绩为: 95.0\n" +
                        "班级排名: 5\n\n" +
                        "你已经掌握基础内容，可以尝试拓展学习。";
        assertEquals(expectedFeedback, grade.getFeedback());
        verify(gradeMapper).update(grade);
    }

    // 测试75-90分的情况
    @Test
    void generateFeedback_ScoreBetween75And90_GeneratesCorrectFeedback() {
        Grade grade = createTestGrade("s002", "c002", 80.0f, 15);
        List<TaskGrade> taskGrades = createPassingTaskGrades();

        when(gradeMapper.findByStudentAndCourse("s002", "c002")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c002")).thenReturn(100.0f);
        when(taskGradeMapper.findByStudentAndCourse("s002", "c002")).thenReturn(taskGrades);
        when(taskMapper.getById(anyString())).thenReturn(createTask(100.0f));

        feedbackService.generateFeedback("s002", "c002");

        String expectedFeedback =
                "你的当前成绩为: 80.0\n" +
                        "班级排名: 15\n\n" +
                        "部分知识点需要巩固，建议重点复习错题。";
        assertEquals(expectedFeedback, grade.getFeedback());
        verify(gradeMapper).update(grade);
    }

    // 测试低于75分的情况
    @Test
    void generateFeedback_ScoreBelow75_GeneratesCorrectFeedback() {
        Grade grade = createTestGrade("s003", "c003", 60.0f, 30);
        List<TaskGrade> taskGrades = createPassingTaskGrades();

        when(gradeMapper.findByStudentAndCourse("s003", "c003")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c003")).thenReturn(100.0f);
        when(taskGradeMapper.findByStudentAndCourse("s003", "c003")).thenReturn(taskGrades);
        when(taskMapper.getById(anyString())).thenReturn(createTask(100.0f));

        feedbackService.generateFeedback("s003", "c003");

        String expectedFeedback =
                "你的当前成绩为: 60.0\n" +
                        "班级排名: 30\n\n" +
                        "你的基础知识点掌握不够牢固，建议复习基础内容。";
        assertEquals(expectedFeedback, grade.getFeedback());
        verify(gradeMapper).update(grade);
    }

    // 测试包含不及格任务的情况
    @Test
    void generateFeedback_WithFailingTasks_IncludesTaskRecommendations() {
        Grade grade = createTestGrade("s004", "c004", 140.0f, 20);

        // 创建任务成绩（两个不及格，一个及格）
        TaskGrade failingTask1 = createTaskGrade("t001", 40.0f);
        TaskGrade passingTask = createTaskGrade("t002", 70.0f);
        TaskGrade failingTask2 = createTaskGrade("t003", 30.0f);
        List<TaskGrade> taskGrades = Arrays.asList(failingTask1, passingTask, failingTask2);

        // 设置任务详情
        Task failingTask1Details = createTask("任务1", 100.0f);
        Task passingTaskDetails = createTask("任务2", 100.0f);
        Task failingTask2Details = createTask("任务3", 50.0f);

        when(gradeMapper.findByStudentAndCourse("s004", "c004")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c004")).thenReturn(250.0f);
        when(taskGradeMapper.findByStudentAndCourse("s004", "c004")).thenReturn(taskGrades);
        when(taskMapper.getById("t001")).thenReturn(failingTask1Details);
        when(taskMapper.getById("t002")).thenReturn(passingTaskDetails);
        when(taskMapper.getById("t003")).thenReturn(failingTask2Details);

        feedbackService.generateFeedback("s004", "c004");

        String expectedFeedback =
                "你的当前成绩为: 140.0\n" +
                        "班级排名: 20\n\n" +
                        "部分知识点需要巩固，建议重点复习错题。" +
                        "\n\n建议重点关注以下任务:\n" +
                        "- 任务1\n" +
                        "- 任务3\n";
        assertEquals(expectedFeedback, grade.getFeedback());
        verify(gradeMapper).update(grade);
    }

    // 测试没有任务成绩记录的情况
    @Test
    void generateFeedback_NoTaskGrades_ThrowsException() {
        Grade grade = createTestGrade("s005", "c005", 85.0f, 10);

        when(gradeMapper.findByStudentAndCourse("s005", "c005")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c005")).thenReturn(100.0f);
        when(taskGradeMapper.findByStudentAndCourse("s005", "c005")).thenReturn(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> feedbackService.generateFeedback("s005", "c005")
        );
        assertEquals("未找到任何任务成绩记录", exception.getMessage());
        verify(gradeMapper, never()).update(any());
    }

    // 测试边界值75分的情况
    @Test
    void generateFeedback_ScoreExactly75_GeneratesCorrectFeedback() {
        Grade grade = createTestGrade("s006", "c006", 75.0f, 12);
        List<TaskGrade> taskGrades = createPassingTaskGrades();

        when(gradeMapper.findByStudentAndCourse("s006", "c006")).thenReturn(grade);
        when(taskMapper.findTotalScoreByCourseId("c006")).thenReturn(100.0f);
        when(taskGradeMapper.findByStudentAndCourse("s006", "c006")).thenReturn(taskGrades);
        when(taskMapper.getById(anyString())).thenReturn(createTask(100.0f));

        feedbackService.generateFeedback("s006", "c006");

        String expectedFeedback =
                "你的当前成绩为: 75.0\n" +
                        "班级排名: 12\n\n" +
                        "部分知识点需要巩固，建议重点复习错题。";
        assertEquals(expectedFeedback, grade.getFeedback());
        verify(gradeMapper).update(grade);
    }

    // ===== 测试辅助方法 =====

    private Grade createTestGrade(String studentId, String courseId, float finalGrade, int rank) {
        Grade grade = new Grade();
        grade.setStudentId(studentId);
        grade.setCourseId(courseId);
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

    private Task createTask(float maxScore) {
        return createTask("测试任务", maxScore);
    }

    private Task createTask(String title, float maxScore) {
        Task task = new Task();
        task.setTaskId("t_default");
        task.setTitle(title);
        task.setMaxScore(maxScore);
        return task;
    }

    private List<TaskGrade> createPassingTaskGrades() {
        TaskGrade taskGrade = createTaskGrade("t_pass", 80.0f);
        return Collections.singletonList(taskGrade);
    }
}
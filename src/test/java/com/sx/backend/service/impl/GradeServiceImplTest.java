package com.sx.backend.service.impl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.AnalysisService;
import com.sx.backend.service.FeedbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class GradeServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;
    @Mock
    private TaskGradeMapper taskGradeMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private AnalysisService analysisService;
    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private GradeServiceImpl gradeService;

    @Captor
    private ArgumentCaptor<TaskGrade> taskGradeCaptor;

    @Captor
    private ArgumentCaptor<Grade> gradeCaptor;

    private final String studentId = "student-001";
    private final String taskId = "task-001";
    private final String courseId = "course-001";
    private Submission submission;
    private TaskGrade existingTaskGrade;
    private Task task;

    @BeforeEach
    void setUp() {
        submission = new Submission();
        submission.setStudentId(studentId);
        submission.setTaskId(taskId);
        submission.setFinalGrade(85.0f);
        submission.setFeedback("Good work");

        existingTaskGrade = new TaskGrade();
        existingTaskGrade.setTaskGradeId("existing-id");
        existingTaskGrade.setStudentId(studentId);
        existingTaskGrade.setTaskId(taskId);
        existingTaskGrade.setCourseId(courseId);
        existingTaskGrade.setScore(80.0f);

        task = new Task();
        task.setTaskId(taskId);
        task.setCourseId(courseId);
    }

    @Test
    void updateTaskGrade_NewTaskGrade_CreatesSuccessfully() {
        when(taskGradeMapper.findByStudentAndTask(studentId, taskId)).thenReturn(null);
        when(taskMapper.getById(taskId)).thenReturn(task);

        gradeService.updateTaskGrade(submission);

        verify(taskGradeMapper).insert(taskGradeCaptor.capture());

        TaskGrade captured = taskGradeCaptor.getValue();
        assertEquals(studentId, captured.getStudentId());
        assertEquals(taskId, captured.getTaskId());
        assertEquals(85.0f, captured.getScore());
        assertNotNull(captured.getTaskGradeId());
        assertEquals(courseId, captured.getCourseId());
        assertNotNull(captured.getGradedTime());

        verify(gradeMapper, atLeastOnce()).update(any());
        verify(analysisService, atLeastOnce()).updateGradeTrend(any(), any());
    }

    @Test
    void updateTaskGrade_ExistingTaskGrade_UpdatesSuccessfully() {
        when(taskGradeMapper.findByStudentAndTask(studentId, taskId)).thenReturn(existingTaskGrade);

        gradeService.updateTaskGrade(submission);

        verify(taskGradeMapper).update(taskGradeCaptor.capture());

        TaskGrade captured = taskGradeCaptor.getValue();
        assertEquals("existing-id", captured.getTaskGradeId());
        assertEquals(85.0f, captured.getScore());
        assertEquals("Good work", captured.getFeedback());
        assertNotNull(captured.getGradedTime());

        verify(gradeMapper, atLeastOnce()).update(any());
        verify(analysisService, atLeastOnce()).updateGradeTrend(any(), any());
    }

    @Test
    void updateFinalGrade_NewGrade_CreatesAndCalculates() {
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null);
        when(taskGradeMapper.findByStudentAndCourse(studentId, courseId))
                .thenReturn(Collections.singletonList(existingTaskGrade));

        gradeService.updateFinalGrade(existingTaskGrade);

        verify(gradeMapper).insert(gradeCaptor.capture());

        Grade capturedGrade = gradeCaptor.getValue();
        assertEquals(80.0f, capturedGrade.getFinalGrade());
        assertNotNull(capturedGrade.getGradeId());
        assertEquals(studentId, capturedGrade.getStudentId());
        assertEquals(courseId, capturedGrade.getCourseId());

        verify(gradeMapper, atLeastOnce()).update(any());
        verify(feedbackService).generateFeedback(studentId, courseId);
        verify(analysisService).updateGradeTrend(studentId, courseId);
    }

    @Test
    void updateFinalGrade_ExistingGrade_UpdatesCorrectly() {
        Grade existingGrade = new Grade();
        existingGrade.setGradeId("grade-001");
        existingGrade.setStudentId(studentId);
        existingGrade.setCourseId(courseId);
        existingGrade.setFinalGrade(75.0f);

        TaskGrade newTaskGrade = new TaskGrade();
        newTaskGrade.setScore(90.0f);
        List<TaskGrade> taskGrades = Arrays.asList(existingTaskGrade, newTaskGrade);

        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(existingGrade);
        when(taskGradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(taskGrades);

        gradeService.updateFinalGrade(existingTaskGrade);

        verify(gradeMapper).update(gradeCaptor.capture());

        Grade updatedGrade = gradeCaptor.getValue();
        assertEquals(170.0f, updatedGrade.getFinalGrade());
    }

    @Test
    void updateFinalGrade_RankingLogic_CorrectOrder() {
        // 准备测试数据
        Grade grade1 = new Grade("id1", studentId, courseId, 90.0f, 0);
        Grade grade2 = new Grade("id2", "student-002", courseId, 85.0f, 0);
        Grade grade3 = new Grade("id3", "student-003", courseId, 90.0f, 0);
        List<Grade> allGrades = Arrays.asList(grade1, grade2, grade3);

        // 模拟依赖
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(grade1);
        when(taskGradeMapper.findByStudentAndCourse(studentId, courseId))
                .thenReturn(Collections.singletonList(existingTaskGrade));
        when(gradeMapper.findByCourseId(courseId)).thenReturn(allGrades);

        gradeService.updateFinalGrade(existingTaskGrade);

        // 不再验证具体调用次数，而是验证排名结果
        // 由于方法内部会修改我们传入的allGrades对象，可以直接验证这些对象
        assertEquals(3, grade1.getRankInClass());
        assertEquals(2, grade2.getRankInClass());
        assertEquals(1, grade3.getRankInClass());
    }

    @Test
    void updateFinalGrade_EmptyTaskGrades_SetsZero() {
        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(null);
        when(taskGradeMapper.findByStudentAndCourse(studentId, courseId))
                .thenReturn(Collections.emptyList());

        gradeService.updateFinalGrade(existingTaskGrade);

        verify(gradeMapper).insert(gradeCaptor.capture());

        Grade capturedGrade = gradeCaptor.getValue();
        assertEquals(0.0f, capturedGrade.getFinalGrade());
    }

    @Test
    void updateFinalGrade_SingleTaskGrade_CalculatesCorrectly() {
        Grade existingGrade = new Grade();
        existingGrade.setGradeId("grade-001");
        existingGrade.setStudentId(studentId);
        existingGrade.setCourseId(courseId);
        existingGrade.setFinalGrade(75.0f);

        when(gradeMapper.findByStudentAndCourse(studentId, courseId)).thenReturn(existingGrade);
        when(taskGradeMapper.findByStudentAndCourse(studentId, courseId))
                .thenReturn(Collections.singletonList(existingTaskGrade));

        gradeService.updateFinalGrade(existingTaskGrade);

        verify(gradeMapper).update(gradeCaptor.capture());

        Grade updatedGrade = gradeCaptor.getValue();
        assertEquals(80.0f, updatedGrade.getFinalGrade());
    }
}
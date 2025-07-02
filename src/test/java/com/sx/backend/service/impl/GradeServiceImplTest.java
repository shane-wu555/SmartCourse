package com.sx.backend.service.impl;

import com.sx.backend.entity.Grade;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskGrade;
import com.sx.backend.mapper.GradeMapper;
import com.sx.backend.mapper.TaskGradeMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.AnalysisService;
import com.sx.backend.service.CourseService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GradeServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private TaskGradeMapper taskGradeMapper;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private AnalysisService analysisService;

    @Mock
    private CourseService courseService; // 虽然未使用但需要模拟

    @InjectMocks
    private GradeServiceImpl gradeService;

    private TaskGrade taskGrade;
    private Task task;
    private Grade existingGrade;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        taskGrade = new TaskGrade();
        taskGrade.setTaskId("task001");
        taskGrade.setStudentId("student001");
        taskGrade.setScore(85.0f);

        task = new Task();
        task.setTaskId("task001");
        task.setCourseId("course001");

        existingGrade = new Grade();
        existingGrade.setGradeId("grade001");
        existingGrade.setCourseId("course001");
        existingGrade.setStudentId("student001");
        existingGrade.setFinalGrade(0.0f);
    }

    @Test
    @Transactional
    void updateTaskGrade_ShouldInsertNewTaskGrade_WhenIdIsNull() {
        // 准备
        taskGrade.setTaskGradeId(null);
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(Collections.emptyList());

        // 执行
        gradeService.updateTaskGrade(taskGrade);

        // 验证
        verify(taskGradeMapper).insert(argThat(tg -> tg.getTaskGradeId() != null)); // 验证ID被设置
        verify(gradeMapper).update(existingGrade); // 验证总成绩更新
        verify(analysisService).updateGradeTrend("course001", "student001"); // 验证趋势分析调用
    }

    @Test
    @Transactional
    void updateTaskGrade_ShouldUpdateExistingTaskGrade_WhenIdExists() {
        // 准备
        taskGrade.setTaskGradeId("tg001");
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(Collections.singletonList(taskGrade));

        // 执行
        gradeService.updateTaskGrade(taskGrade);

        // 验证
        verify(taskGradeMapper).update(taskGrade); // 验证更新操作
        verify(taskGradeMapper, never()).insert(any()); // 验证未执行插入
        verify(gradeMapper).update(existingGrade); // 验证总成绩更新
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldCreateNewGrade_WhenNotExists() {
        // 准备
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(null);
        when(taskGradeMapper.findByGradeId(anyString())).thenReturn(Collections.singletonList(taskGrade));

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证
        verify(gradeMapper).insert(argThat(grade ->
                grade.getGradeId() != null &&
                        grade.getCourseId().equals("course001") &&
                        grade.getStudentId().equals("student001")
        ));
        verify(gradeMapper).update(any(Grade.class)); // 验证更新新创建的成绩
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldCalculateCorrectTotalScore() {
        // 准备
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);

        // 创建多个任务成绩
        TaskGrade tg1 = new TaskGrade();
        tg1.setScore(80.0f);
        TaskGrade tg2 = new TaskGrade();
        tg2.setScore(90.0f);
        List<TaskGrade> taskGrades = Arrays.asList(taskGrade, tg1, tg2);

        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(taskGrades);

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证总成绩计算正确 (85 + 80 + 90 = 255)
        verify(gradeMapper).update(argThat(grade ->
                Math.abs(grade.getFinalGrade() - 255.0f) < 0.001
        ));
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldHandleZeroTasks() {
        // 准备
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(Collections.emptyList());

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证总成绩为0
        verify(gradeMapper).update(argThat(grade ->
                Math.abs(grade.getFinalGrade() - 0.0f) < 0.001
        ));
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldUpdateExistingGrade() {
        // 准备
        existingGrade.setFinalGrade(100.0f); // 初始成绩
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(Collections.singletonList(taskGrade));

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证
        verify(gradeMapper, never()).insert(any()); // 验证未创建新成绩
        verify(gradeMapper).update(existingGrade); // 验证更新了现有成绩
        assertEquals(85.0f, existingGrade.getFinalGrade()); // 验证成绩更新为最新值
    }

    @Test
    @Transactional
    void updateTaskGrade_ShouldCallAnalysisService() {
        // 准备
        taskGrade.setTaskGradeId(null);
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(Collections.singletonList(taskGrade));

        // 执行
        gradeService.updateTaskGrade(taskGrade);

        // 验证
        verify(analysisService).updateGradeTrend("course001", "student001");
    }

    @Test
    @Transactional
    void updateTaskGrade_ShouldHandleMultipleTaskGrades() {
        // 准备
        taskGrade.setTaskGradeId("tg001");
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(existingGrade);

        // 创建多个任务成绩
        TaskGrade tg1 = new TaskGrade();
        tg1.setScore(70.0f);
        TaskGrade tg2 = new TaskGrade();
        tg2.setScore(95.0f);
        List<TaskGrade> taskGrades = Arrays.asList(taskGrade, tg1, tg2);
        when(taskGradeMapper.findByGradeId("grade001")).thenReturn(taskGrades);

        // 执行
        gradeService.updateTaskGrade(taskGrade);

        // 验证总成绩计算正确 (85 + 70 + 95 = 250)
        verify(gradeMapper).update(argThat(grade ->
                Math.abs(grade.getFinalGrade() - 250.0f) < 0.001
        ));
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldCreateGradeWithCorrectUUID() {
        // 准备
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(null);
        when(taskGradeMapper.findByGradeId(anyString())).thenReturn(Collections.singletonList(taskGrade));

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证新成绩的ID是有效的UUID
        verify(gradeMapper).insert(argThat(grade -> {
            try {
                UUID.fromString(grade.getGradeId());
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }));
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldHandleNullTask() {
        // 准备
        when(taskMapper.getById("task001")).thenReturn(null); // 任务不存在

        // 执行和验证异常
        assertThrows(NullPointerException.class, () ->
                gradeService.updateFinalGrade(taskGrade)
        );
    }

    @Test
    @Transactional
    void updateTaskGrade_ShouldNotUpdateTrend_WhenTaskNotFound() {
        // 准备
        taskGrade.setTaskGradeId(null);
        when(taskMapper.getById("task001")).thenReturn(null); // 任务不存在

        // 执行和验证
        assertThrows(NullPointerException.class, () ->
                gradeService.updateTaskGrade(taskGrade)
        );
        verifyNoInteractions(analysisService); // 确保未调用趋势分析
    }

    @Test
    @Transactional
    void updateFinalGrade_ShouldRecalculateAfterInsert() {
        // 准备 - 新成绩
        when(taskMapper.getById("task001")).thenReturn(task);
        when(gradeMapper.findByStudentAndCourse("course001", "student001")).thenReturn(null);

        // 添加两个任务成绩（新插入的当前成绩+已有的另一个成绩）
        TaskGrade existingTaskGrade = new TaskGrade();
        existingTaskGrade.setScore(75.0f);
        when(taskGradeMapper.findByGradeId(anyString())).thenReturn(Arrays.asList(taskGrade, existingTaskGrade));

        // 执行
        gradeService.updateFinalGrade(taskGrade);

        // 验证总成绩计算正确 (85 + 75 = 160)
        verify(gradeMapper).update(argThat(grade ->
                Math.abs(grade.getFinalGrade() - 160.0f) < 0.001
        ));
    }
}
package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sx.backend.dto.AnswerRecordDTO;
import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.*;
import com.sx.backend.mapper.*;
import com.sx.backend.service.GradingService;
import org.apache.poi.ss.formula.functions.T;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class SubmissionServiceImplTest {

    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private AnswerRecordMapper answerRecordMapper;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private GradingService gradingService;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private SubmissionDTO validSubmissionDTO;
    private SubmissionDTO fileSubmissionDTO;

    @BeforeEach
    void setUp() {
        // 初始化有效答题提交DTO
        validSubmissionDTO = new SubmissionDTO();
        validSubmissionDTO.setTaskId("task-1");
        validSubmissionDTO.setStudentId("student-1");
        validSubmissionDTO.setSubmitTime(LocalDateTime.now());

        AnswerRecordDTO recordDTO = new AnswerRecordDTO();
        recordDTO.setQuestionId("question-1");
        recordDTO.setAnswers(Arrays.asList("A", "B"));
        validSubmissionDTO.setAnswerRecordDTO(Collections.singletonList(recordDTO));

        // 初始化文件提交DTO
        fileSubmissionDTO = new SubmissionDTO();
        fileSubmissionDTO.setTaskId("task-2");
        fileSubmissionDTO.setStudentId("student-2");
        fileSubmissionDTO.setSubmitTime(LocalDateTime.now());
        fileSubmissionDTO.setFileId(Arrays.asList("file1", "file2"));
    }

    // submitAnswerRecords 测试案例
    @Test
    void submitAnswerRecords_InvalidTaskId_ThrowsException() {
        when(taskMapper.getById("task-1")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitAnswerRecords(validSubmissionDTO));
        assertEquals("Invalid task ID: task-1", exception.getMessage());
    }

    @Test
    void submitAnswerRecords_InvalidStudentId_ThrowsException() {
        when(taskMapper.getById("task-1")).thenReturn(new Task()); // 模拟有效任务
        when(studentMapper.selectById("student-1")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitAnswerRecords(validSubmissionDTO));
        assertEquals("Invalid student ID: student-1", exception.getMessage());
    }

    @Test
    void submitAnswerRecords_NullAnswerRecords_ThrowsException() {
        validSubmissionDTO.setAnswerRecordDTO(null);
        when(taskMapper.getById("task-1")).thenReturn(new Task());
        when(studentMapper.selectById("student-1")).thenReturn(new Student());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitAnswerRecords(validSubmissionDTO));
        assertEquals("Invalid answer record DTO", exception.getMessage());
    }

    @Test
    void submitAnswerRecords_InvalidQuestionId_ThrowsException() {
        when(taskMapper.getById("task-1")).thenReturn(new Task());
        when(studentMapper.selectById("student-1")).thenReturn(new Student());
        when(questionMapper.selectQuestionById("question-1")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitAnswerRecords(validSubmissionDTO));
        assertEquals("Invalid question ID: question-1", exception.getMessage());
    }

    @Test
    void submitAnswerRecords_ValidSubmission_CreatesRecords() {
        // 模拟依赖返回有效对象
        when(taskMapper.getById("task-1")).thenReturn(new Task());
        when(studentMapper.selectById("student-1")).thenReturn(new Student());
        Question question = new Question();
        question.setScore(5.0f);
        when(questionMapper.selectQuestionById("question-1")).thenReturn(question);

        // 捕获生成的Submission和AnswerRecord
        Submission capturedSubmission = new Submission();
        doAnswer(invocation -> {
            Submission sub = invocation.getArgument(0);
            capturedSubmission.setSubmissionId(sub.getSubmissionId());
            return null;
        }).when(submissionMapper).create(any(Submission.class));

        when(submissionMapper.findById(anyString())).thenReturn(capturedSubmission);

        // 执行方法
        Submission result = submissionService.submitAnswerRecords(validSubmissionDTO);

        // 验证流程
        verify(submissionMapper, times(1)).create(any(Submission.class));
        verify(answerRecordMapper, times(1)).create(any(AnswerRecord.class));
        verify(submissionMapper, times(1)).updateCompletedToTrue(anyString());
        verify(gradingService, times(1)).autoGradeSubmission(anyString());
        assertNotNull(result.getSubmissionId());
    }

    // submitFiles 测试案例
    @Test
    void submitFiles_InvalidTaskId_ThrowsException() {
        when(taskMapper.getById("task-2")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitFiles(fileSubmissionDTO));
        assertEquals("Invalid task ID: task-2", exception.getMessage());
    }

    @Test
    void submitFiles_InvalidStudentId_ThrowsException() {
        when(taskMapper.getById("task-2")).thenReturn(new Task());
        when(studentMapper.selectById("student-2")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitFiles(fileSubmissionDTO));
        assertEquals("Invalid student ID: student-2", exception.getMessage());
    }

    @Test
    void submitFiles_EmptyFileId_ThrowsException() {
        fileSubmissionDTO.setFileId(Collections.singletonList(""));
        when(taskMapper.getById("task-2")).thenReturn(new Task());
        when(studentMapper.selectById("student-2")).thenReturn(new Student());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> submissionService.submitFiles(fileSubmissionDTO));
        assertEquals("Invalid file ID: ", exception.getMessage());
    }

    @Test
    void submitFiles_ValidSubmission_CreatesRecords() {
        when(taskMapper.getById("task-2")).thenReturn(new Task());
        when(studentMapper.selectById("student-2")).thenReturn(new Student());
        when(submissionMapper.create(any(Submission.class))).thenReturn(1);

        Submission result = submissionService.submitFiles(fileSubmissionDTO);

        verify(submissionMapper, times(1)).create(any(Submission.class));
        verify(submissionMapper, times(2)).insertFile(anyString(), anyString(), anyString());
        assertNotNull(result.getSubmissionId());
        assertEquals(2, result.getFiles().size());
    }
}
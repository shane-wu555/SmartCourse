package com.sx.backend.controller;

import com.sx.backend.dto.AnswerRecordDTO;
import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.SubmissionStatus;
import com.sx.backend.entity.Task;
import com.sx.backend.entity.TaskType;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    @Mock
    private SubmissionMapper submissionMapper;

    @Mock
    private SubmissionService submissionService;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SubmissionController submissionController;

    private final String studentId = "student-123";
    private final String taskId = "task-456";
    private final String courseId = "course-789";

    @BeforeEach
    void setUp() {
        when(request.getAttribute("userId")).thenReturn(studentId);
        submissionController.storageLocation = "/test/storage";
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getSubmissionsByCourse_ValidRequest_ShouldReturnSubmissions() {
        // Arrange
        List<Submission> expected = Collections.singletonList(new Submission());
        when(submissionMapper.findByCourseIdAndStudentId(courseId, studentId)).thenReturn(expected);

        // Act
        List<Submission> result = submissionController.getSubmissionsByCourse(courseId);

        // Assert
        assertEquals(expected, result);
    }

    @Test
    void completeSubmission_ExistingCompleted_ShouldReturnSameSubmission() {
        // Arrange
        Submission existing = new Submission();
        existing.setCompleted(true);
        when(submissionMapper.findByTaskIdAndStudentId(taskId, studentId)).thenReturn(existing);

        // Act
        ResponseEntity<ApiResponse<Submission>> response = submissionController.completeSubmission(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isCompleted());
        verify(submissionMapper, never()).create(any());
        verify(submissionMapper, never()).update(any());
    }

    @Test
    void completeSubmission_NewSubmission_ShouldCreateAndReturn() {
        // Arrange
        when(submissionMapper.findByTaskIdAndStudentId(taskId, studentId)).thenReturn(null);
        when(taskMapper.getById(taskId)).thenReturn(new Task());
        when(submissionMapper.create(any())).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            s.setSubmissionId(UUID.randomUUID().toString());
            return 1;
        });

        // Act
        ResponseEntity<ApiResponse<Submission>> response = submissionController.completeSubmission(taskId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().getData().getSubmissionId());
        assertEquals(SubmissionStatus.GRADED, response.getBody().getData().getStatus());
        verify(submissionMapper).create(any(Submission.class));
    }

    @Test
    void submitFiles_ValidRequest_ShouldReturnCreated() throws Exception {
        // Arrange
        MultipartFile file = new MockMultipartFile("test.txt", "Hello".getBytes());
        SubmissionDTO dto = new SubmissionDTO();
        dto.setTaskId(taskId);
        Submission submission = new Submission();

        when(submissionService.submitFiles(any())).thenReturn(submission);

        // Act
        ResponseEntity<ApiResponse<Submission>> response = submissionController.submitFiles(
                taskId,
                Collections.singletonList(file)
        );

        assertEquals(submission, response.getBody().getData());
    }

    @Test
    void submitAnswers_ValidQuiz_ShouldReturnCreated() {
        // Arrange
        AnswerRecordDTO record = new AnswerRecordDTO();
        SubmissionDTO dto = new SubmissionDTO();
        dto.setTaskId(taskId);
        dto.setAnswerRecordDTO(Collections.singletonList(record));

        TaskType quizType = TaskType.EXAM_QUIZ;
        when(taskMapper.getById(taskId)).thenReturn(new Task("task-456", "Quiz", TaskType.EXAM_QUIZ, 100));
        when(submissionService.submitAnswerRecords(any())).thenReturn(new Submission());

        // Act
        ResponseEntity<ApiResponse<Submission>> response = submissionController.submitAnswers(dto);
        assertEquals("提交成功", response.getBody().getMessage());
    }

    @Test
    void submitAnswers_WrongTaskType_ShouldReturnError() {
        // Arrange
        SubmissionDTO dto = new SubmissionDTO();
        dto.setTaskId(taskId);
        dto.setAnswerRecordDTO(Collections.singletonList(new AnswerRecordDTO()));

        when(taskMapper.getById(taskId)).thenReturn(new Task("task-456", "Assignment", TaskType.CHAPTER_HOMEWORK, 100));

        // Act
        ResponseEntity<ApiResponse<Submission>> response = submissionController.submitAnswers(dto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("任务类型不是试卷答题"));
    }
}

package com.sx.backend.controller;

import com.sx.backend.dto.request.ManualGradingRequest;
import com.sx.backend.entity.AnswerRecord;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.SubmissionStatus;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradeService;
import com.sx.backend.service.GradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.springframework.http.ResponseEntity;

class GradingControllerTest {

    private GradingController controller;
    private AnswerRecordMapper answerRecordMapper;
    private SubmissionMapper submissionMapper;
    private GradingService gradingService;
    private GradeService gradeService;

    private Submission mockSubmission;

    @BeforeEach
    void setUp() {
        answerRecordMapper = mock(AnswerRecordMapper.class);
        submissionMapper = mock(SubmissionMapper.class);
        gradingService = mock(GradingService.class);
        gradeService = mock(GradeService.class);

        controller = new GradingController();
        // 反射注入依赖
        setField(controller, "answerRecordMapper", answerRecordMapper);
        setField(controller, "submissionMapper", submissionMapper);
        setField(controller, "gradingService", gradingService);
        setField(controller, "gradeService", gradeService);

        mockSubmission = new Submission();
        mockSubmission.setSubmissionId("sub123");
        mockSubmission.setAnswerRecords(List.of("r1", "r2"));
        mockSubmission.setStatus(SubmissionStatus.SUBMITTED);
    }

    @Test
    void testGetManualQuestions() {
        AnswerRecord r1 = new AnswerRecord();
        AnswerRecord r2 = new AnswerRecord();
        when(submissionMapper.findById("sub123")).thenReturn(mockSubmission);
        when(answerRecordMapper.findById("r1")).thenReturn(r1);
        when(answerRecordMapper.findById("r2")).thenReturn(r2);
        when(gradingService.getQuestionForManualGrading(anyList())).thenReturn(List.of(r1, r2));

        ResponseEntity<List<AnswerRecord>> resp = controller.getManualQuestions("sub123");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().size());
    }

    @Test
    void testSubmitManualGrading_found() {
        ManualGradingRequest req = new ManualGradingRequest();
        req.setFeedback("Good job");
        req.setQuestionGrades(List.of());

        Submission graded = new Submission();
        graded.setSubmissionId("sub123");

        when(submissionMapper.findById("sub123")).thenReturn(mockSubmission);
        when(gradingService.manualGradeSubmission(eq("sub123"), any(), any())).thenReturn(graded);
        when(submissionMapper.update(any())).thenReturn(1);

        ResponseEntity<Submission> resp = controller.submitManualGrading("sub123", req);
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("sub123", resp.getBody().getSubmissionId());
    }

    @Test
    void testSubmitManualGrading_notFound() {
        when(submissionMapper.findById("notfound")).thenReturn(null);
        ManualGradingRequest req = new ManualGradingRequest();
        ResponseEntity<Submission> resp = controller.submitManualGrading("notfound", req);
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testAutoGradeSubmission_found() {
        when(submissionMapper.findById("sub123")).thenReturn(mockSubmission);
        doNothing().when(gradingService).autoGradeSubmission("sub123");

        ResponseEntity<Submission> resp = controller.autoGradeSubmission("sub123");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("sub123", resp.getBody().getSubmissionId());
    }

    @Test
    void testAutoGradeSubmission_notFound() {
        when(submissionMapper.findById("notfound")).thenReturn(null);
        doNothing().when(gradingService).autoGradeSubmission("notfound");

        ResponseEntity<Submission> resp = controller.autoGradeSubmission("notfound");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void testGetWorksForGrading_noContent() {
        when(submissionMapper.findByTaskId("task001")).thenReturn(List.of());
        ResponseEntity<List<Submission>> resp = controller.getWorksForGrading("task001");
        assertEquals(204, resp.getStatusCodeValue());
    }

    @Test
    void testGetWorksForGrading_ok() {
        Submission s1 = new Submission();
        s1.setStatus(SubmissionStatus.SUBMITTED);
        Submission s2 = new Submission();
        s2.setStatus(SubmissionStatus.GRADED);
        when(submissionMapper.findByTaskId("task001")).thenReturn(List.of(s1, s2));
        ResponseEntity<List<Submission>> resp = controller.getWorksForGrading("task001");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
    }

    @Test
    void testSubmitWorkGrading_found() {
        when(submissionMapper.findById("sub123")).thenReturn(mockSubmission);
        when(submissionMapper.update(any())).thenReturn(1);
        doNothing().when(gradeService).updateTaskGrade(any());

        ResponseEntity<Submission> resp = controller.submitWorkGrading("sub123", 95f, "well done");
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void testSubmitWorkGrading_notFound() {
        when(submissionMapper.findById("notfound")).thenReturn(null);
        ResponseEntity<Submission> resp = controller.submitWorkGrading("notfound", 95f, "well done");
        assertEquals(404, resp.getStatusCodeValue());
    }

    // 反射工具方法
    private static void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
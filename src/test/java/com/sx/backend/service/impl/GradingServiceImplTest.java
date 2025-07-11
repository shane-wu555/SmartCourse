package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.entity.ManualGrade;
import com.sx.backend.entity.*;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradeService;
import com.sx.backend.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class GradingServiceImplTest {

    @Mock
    private SubmissionMapper submissionMapper;
    @Mock
    private AnswerRecordMapper answerRecordMapper;
    @Mock
    private QuestionMapper questionMapper;
    @Mock
    private GradeService gradeService;

    @InjectMocks
    private GradingServiceImpl gradingService;

    private Submission submission;
    private List<AnswerRecord> answerRecords;
    private List<String> recordIds;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        recordIds = Arrays.asList("record1", "record2");
        submission = new Submission();
        submission.setSubmissionId("sub1");
        submission.setAnswerRecords(recordIds);
        submission.setStatus(SubmissionStatus.SUBMITTED);

        answerRecords = new ArrayList<>();
        AnswerRecord record1 = new AnswerRecord();
        record1.setRecordId("record1");
        record1.setQuestionId("q1");
        record1.setAnswers(Arrays.asList("A", "B"));

        AnswerRecord record2 = new AnswerRecord();
        record2.setRecordId("record2");
        record2.setQuestionId("q2");
        record2.setAnswers(Arrays.asList("True"));

        answerRecords.add(record1);
        answerRecords.add(record2);
    }

    // 测试自动批改提交
    @Test
    void autoGradeSubmission_shouldUpdateRecordsAndSubmission() {
        // 模拟依赖行为
        when(submissionMapper.findById("sub1")).thenReturn(submission);
        when(answerRecordMapper.findById("record1")).thenReturn(answerRecords.get(0));
        when(answerRecordMapper.findById("record2")).thenReturn(answerRecords.get(1));

        Question q1 = new Question();
        q1.setQuestionId("q1");
        q1.setType(QuestionType.SINGLE_CHOICE);
        q1.setScore(5.0f); // 假设满分5分
        when(questionMapper.selectQuestionById("q1")).thenReturn(q1);

        Question q2 = new Question();
        q2.setQuestionId("q2");
        q2.setType(QuestionType.PROGRAMMING);
        when(questionMapper.selectQuestionById("q2")).thenReturn(q2);

        // 模拟自动评分逻辑
        doAnswer(invocation -> {
            AnswerRecord record = invocation.getArgument(0);
            if (record.getQuestionId().equals("q1"))   // q1是单选题
                    {
                        record.setObtainedScore(4.0f); // 模拟q1得4分
                        record.setAutoGraded(true);
                    } else if (record.getQuestionId().equals("q2"))  // q2是编程题
                    {
                        record.setObtainedScore(0.0f); // 编程题不自动评分
                        record.setAutoGraded(false);
                    }
            return null;
        }).when(answerRecordMapper).update(any(AnswerRecord.class));

        // 执行测试
        gradingService.autoGradeSubmission("sub1");

        // 验证结果
        verify(answerRecordMapper).update(answerRecords.get(0));
        verify(answerRecordMapper).update(answerRecords.get(1));
        assertEquals(4.0f, answerRecords.get(0).getObtainedScore());
        assertEquals(0.0f, answerRecords.get(1).getObtainedScore());
        assertTrue(answerRecords.get(0).isAutoGraded());
        assertFalse(answerRecords.get(1).isAutoGraded());
        verify(submissionMapper).update(submission);
        assertEquals(SubmissionStatus.AUTO_GRADED, submission.getStatus());
        verify(gradeService).updateTaskGrade(submission);
        assertNotNull(submission.getGradeTime());
    }

    // 测试手动批改提交
    @Test
    void manualGradeSubmission_shouldUpdateManualGrades() {
        // 准备手动批改数据
        submission.setAutoGrade(3.0f); // 已有自动评分
        submission.setStatus(SubmissionStatus.AUTO_GRADED);

        Question autoGradedQuestion = new Question();
        autoGradedQuestion.setQuestionId("q1");
        autoGradedQuestion.setType(QuestionType.SINGLE_CHOICE); // 自动评分类型

        when(questionMapper.selectQuestionById("q1")).thenReturn(autoGradedQuestion);
        when(submissionMapper.findById("sub1")).thenReturn(submission);
        when(answerRecordMapper.findById("record1")).thenReturn(answerRecords.get(0));
        when(answerRecordMapper.findById("record2")).thenReturn(answerRecords.get(1));
        Question manualQuestion = new Question();
        manualQuestion.setQuestionId("q2");
        manualQuestion.setType(QuestionType.SHORT_ANSWER);  // 确保是手动批改类型
        when(questionMapper.selectQuestionById("q2")).thenReturn(manualQuestion);

        // 配置需要手动批改的记录（假设record2需要手动批改）
        answerRecords.get(0).setAutoGraded(true); // q1已自动批改
        answerRecords.get(1).setAutoGraded(false); // q2需要手动批改

        // 手动评分数据
        ManualGrade manualGrade = new ManualGrade();
        manualGrade.setRecordId("record2");
        manualGrade.setScore(2.0f); // 手动给2分
        manualGrade.setFeedback("Good answer");
        List<ManualGrade> manualGrades = Collections.singletonList(manualGrade);

        // 执行手动批改
        Submission result = gradingService.manualGradeSubmission("sub1", manualGrades, "Overall feedback");

        // 验证结果
        assertEquals(5.0f, result.getFinalGrade()); // 3 + 2 = 5
        assertEquals("Overall feedback", result.getFeedback());
        assertEquals(SubmissionStatus.GRADED, result.getStatus());
        verify(answerRecordMapper).update(answerRecords.get(1));
        assertEquals(2.0f, answerRecords.get(1).getObtainedScore());
        assertEquals("Good answer", answerRecords.get(1).getTeacherFeedback());
        verify(gradeService).updateTaskGrade(submission);
    }

    @Test
    void manualGrading_whenQuestionNotFound_shouldThrowException() {
        AnswerRecord record = new AnswerRecord();
        record.setQuestionId("invalid_q");

        when(questionMapper.selectQuestionById("invalid_q")).thenReturn(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                gradingService.manualGrading(record, 5.0f, "Feedback")
        );
        assertTrue(exception.getMessage().contains("invalid_q"));
    }

    // 测试缺少手动评分记录时的异常
    @Test
    void manualGradeSubmission_missingManualGrade_shouldThrowException() {
        submission.setAutoGrade(3.0f);
        when(submissionMapper.findById("sub1")).thenReturn(submission);

        // 设置 questionMapper 返回的题型
        Question q1 = new Question();
        q1.setQuestionId("q1");
        q1.setType(QuestionType.SINGLE_CHOICE); // 自动评分题型
        when(questionMapper.selectQuestionById("q1")).thenReturn(q1);

        Question q2 = new Question();
        q2.setQuestionId("q2");
        q2.setType(QuestionType.SHORT_ANSWER); // 手动评分题型
        when(questionMapper.selectQuestionById("q2")).thenReturn(q2);

        // 设置 AnswerRecord mock 返回
        when(answerRecordMapper.findById(anyString())).thenAnswer(inv ->
                answerRecords.stream()
                        .filter(r -> r.getRecordId().equals(inv.getArgument(0)))
                        .findFirst()
                        .orElse(null)
        );

        // 设置 autoGraded 标志
        answerRecords.get(0).setAutoGraded(true);  // q1 自动批改
        answerRecords.get(1).setAutoGraded(false); // q2 需要手动批改

        // 模拟没有提供手动评分
        List<ManualGrade> manualGrades = Collections.emptyList();

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                gradingService.manualGradeSubmission("sub1", manualGrades, "Feedback")
        );

        assertTrue(exception.getMessage().contains("record2"));
    }

    // 测试获取手动批改题目
    @Test
    void getQuestionForManualGrading_shouldFilterAutoGraded() {
        // 设置批改状态
        Question q1 = new Question();
        q1.setQuestionId("q1");
        q1.setType(QuestionType.SINGLE_CHOICE);
        q1.setScore(5.0f); // 假设满分5分
        when(questionMapper.selectQuestionById("q1")).thenReturn(q1);

        Question q2 = new Question();
        q2.setQuestionId("q2");
        q2.setType(QuestionType.PROGRAMMING);
        when(questionMapper.selectQuestionById("q2")).thenReturn(q2);

        List<AnswerRecord> result = gradingService.getQuestionForManualGrading(answerRecords);

        assertEquals(1, result.size());
        assertEquals("record2", result.get(0).getRecordId());
    }

    // 测试手动批改单个题目（异常场景：尝试批改自动评分题目）
    @Test
    void manualGrading_onAutoGradableQuestion_shouldThrowException() {
        AnswerRecord record = new AnswerRecord();
        record.setQuestionId("q1");

        Question question = new Question();
        question.setType(QuestionType.MULTIPLE_CHOICE);
        when(questionMapper.selectQuestionById("q1")).thenReturn(question);

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                gradingService.manualGrading(record, 5.0f, "Feedback")
        );
        assertTrue(exception.getMessage().contains("题目类型为 MULTIPLE_CHOICE 的题目应该自动评分，不能手动评分"));
    }
}
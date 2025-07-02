package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.entity.DifficultyLevel;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.QuestionType;
import com.sx.backend.mapper.QuestionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private static final String QUESTION_ID = "q-001";
    private static final String BANK_ID = "bank-001";
    private Question question;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setQuestionId(QUESTION_ID);
        question.setBankId(BANK_ID);
        question.setType(QuestionType.MULTIPLE_CHOICE);
        question.setDifficultylevel(DifficultyLevel.MEDIUM);
        question.setContent("What is Java?");
        question.setOptions(Arrays.asList("A", "B", "C"));
        question.setAnswer("B");
    }

    @Test
    void addQuestion_Success() {
        // 模拟行为
        when(questionMapper.insertQuestion(any(Question.class))).thenReturn(1);

        // 执行方法
        int result = questionService.addQuestion(question);

        // 验证结果
        assertEquals(1, result);
        verify(questionMapper).insertQuestion(question);
    }

    @Test
    void addQuestion_Failure() {
        // 模拟行为
        when(questionMapper.insertQuestion(any(Question.class))).thenReturn(0);

        // 执行方法
        int result = questionService.addQuestion(question);

        // 验证结果
        assertEquals(0, result);
        verify(questionMapper).insertQuestion(question);
    }

    @Test
    void updateQuestion_Success() {
        // 准备更新数据
        Question updatedQuestion = new Question();
        updatedQuestion.setQuestionId(QUESTION_ID);
        updatedQuestion.setContent("Updated question content");

        // 模拟行为
        when(questionMapper.updateQuestion(any(Question.class))).thenReturn(1);

        // 执行方法
        int result = questionService.updateQuestion(updatedQuestion);

        // 验证结果
        assertEquals(1, result);
        verify(questionMapper).updateQuestion(updatedQuestion);
    }

    @Test
    void updateQuestion_Failure() {
        // 准备更新数据
        Question updatedQuestion = new Question();
        updatedQuestion.setQuestionId(QUESTION_ID);
        updatedQuestion.setContent("Updated question content");

        // 模拟行为
        when(questionMapper.updateQuestion(any(Question.class))).thenReturn(0);

        // 执行方法
        int result = questionService.updateQuestion(updatedQuestion);

        // 验证结果
        assertEquals(0, result);
        verify(questionMapper).updateQuestion(updatedQuestion);
    }

    @Test
    void deleteQuestion_Success() {
        // 模拟行为
        when(questionMapper.deleteQuestion(QUESTION_ID)).thenReturn(1);

        // 执行方法
        int result = questionService.deleteQuestion(QUESTION_ID);

        // 验证结果
        assertEquals(1, result);
        verify(questionMapper).deleteQuestion(QUESTION_ID);
    }

    @Test
    void deleteQuestion_Failure() {
        // 模拟行为
        when(questionMapper.deleteQuestion(QUESTION_ID)).thenReturn(0);

        // 执行方法
        int result = questionService.deleteQuestion(QUESTION_ID);

        // 验证结果
        assertEquals(0, result);
        verify(questionMapper).deleteQuestion(QUESTION_ID);
    }

    @Test
    void getQuestionById_Exists() {
        // 模拟行为
        when(questionMapper.selectQuestionById(QUESTION_ID)).thenReturn(question);

        // 执行方法
        Question result = questionService.getQuestionById(QUESTION_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(QUESTION_ID, result.getQuestionId());
        assertEquals("What is Java?", result.getContent());
        verify(questionMapper).selectQuestionById(QUESTION_ID);
    }

    @Test
    void getQuestionById_NotExists() {
        // 模拟行为
        when(questionMapper.selectQuestionById(QUESTION_ID)).thenReturn(null);

        // 执行方法
        Question result = questionService.getQuestionById(QUESTION_ID);

        // 验证结果
        assertNull(result);
        verify(questionMapper).selectQuestionById(QUESTION_ID);
    }

    @Test
    void getQuestionsByBankId_WithData() {
        // 准备数据
        Question question2 = new Question();
        question2.setQuestionId("q-002");

        List<Question> questions = Arrays.asList(question, question2);

        // 模拟行为
        when(questionMapper.selectQuestionsByBankId(BANK_ID)).thenReturn(questions);

        // 执行方法
        List<Question> result = questionService.getQuestionsByBankId(BANK_ID);

        // 验证结果
        assertEquals(2, result.size());
        verify(questionMapper).selectQuestionsByBankId(BANK_ID);
    }

    @Test
    void getQuestionsByBankId_Empty() {
        // 模拟行为
        when(questionMapper.selectQuestionsByBankId(BANK_ID)).thenReturn(Collections.emptyList());

        // 执行方法
        List<Question> result = questionService.getQuestionsByBankId(BANK_ID);

        // 验证结果
        assertTrue(result.isEmpty());
        verify(questionMapper).selectQuestionsByBankId(BANK_ID);
    }

    @Test
    void getQuestionsByCondition_AllConditions() {
        // 准备数据
        String type = "MULTIPLE_CHOICE";
        String difficulty = "MEDIUM";
        List<String> knowledgePoints = Arrays.asList("kp-001", "kp-002");

        // 模拟行为
        when(questionMapper.selectQuestionsByCondition(type, difficulty, knowledgePoints))
                .thenReturn(Collections.singletonList(question));

        // 执行方法
        List<Question> result = questionService.getQuestionsByCondition(type, difficulty, knowledgePoints);

        // 验证结果
        assertEquals(1, result.size());
        verify(questionMapper).selectQuestionsByCondition(type, difficulty, knowledgePoints);
    }

    @Test
    void getQuestionsByCondition_PartialConditions() {
        // 准备数据
        String type = "MULTIPLE_CHOICE";
        String difficulty = null;
        List<String> knowledgePoints = null;

        // 模拟行为
        when(questionMapper.selectQuestionsByCondition(type, difficulty, knowledgePoints))
                .thenReturn(Collections.singletonList(question));

        // 执行方法
        List<Question> result = questionService.getQuestionsByCondition(type, difficulty, knowledgePoints);

        // 验证结果
        assertEquals(1, result.size());
        verify(questionMapper).selectQuestionsByCondition(type, difficulty, knowledgePoints);
    }

    @Test
    void batchAddQuestions_Success() {
        // 准备数据
        Question question2 = new Question();
        question2.setQuestionId("q-002");

        List<Question> questions = Arrays.asList(question, question2);

        // 模拟行为
        when(questionMapper.batchInsertQuestions(questions)).thenReturn(2);

        // 执行方法
        int result = questionService.batchAddQuestions(questions);

        // 验证结果
        assertEquals(2, result);
        verify(questionMapper).batchInsertQuestions(questions);
    }

    @Test
    void batchAddQuestions_PartialSuccess() {
        // 准备数据
        Question question2 = new Question();
        question2.setQuestionId("q-002");

        List<Question> questions = Arrays.asList(question, question2);

        // 模拟行为
        when(questionMapper.batchInsertQuestions(questions)).thenReturn(1);

        // 执行方法
        int result = questionService.batchAddQuestions(questions);

        // 验证结果
        assertEquals(1, result);
        verify(questionMapper).batchInsertQuestions(questions);
    }

    @Test
    void batchAddQuestions_EmptyList() {
        // 准备数据
        List<Question> questions = Collections.emptyList();

        // 执行方法
        int result = questionService.batchAddQuestions(questions);

        // 验证结果
        assertEquals(0, result);
        verify(questionMapper, never()).batchInsertQuestions(anyList());
    }
}

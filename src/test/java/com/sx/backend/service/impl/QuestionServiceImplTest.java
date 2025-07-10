package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.sx.backend.entity.Question;
import com.sx.backend.entity.QuestionType;
import com.sx.backend.mapper.QuestionMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionMapper questionMapper;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private Question validQuestion;
    private List<Question> validQuestions;

    @BeforeEach
    void setUp() {
        validQuestion = new Question();
        validQuestion.setQuestionId("Q1");
        validQuestion.setBankId("B1");
        validQuestion.setContent("What is Java?");
        validQuestion.setType(QuestionType.MULTIPLE_CHOICE);

        validQuestions = Arrays.asList(
                createValidQuestion("Q1"),
                createValidQuestion("Q2")
        );
    }

    private Question createValidQuestion(String id) {
        Question q = new Question();
        q.setQuestionId(id);
        q.setBankId("B" + id.charAt(1));
        q.setContent("Content for " + id);
        q.setType(QuestionType.SHORT_ANSWER);
        return q;
    }

    // addQuestion 测试
    @Test
    void addQuestion_ValidInput_ReturnsSuccess() {
        when(questionMapper.insertQuestion(any(Question.class))).thenReturn(1);
        assertEquals(1, questionService.addQuestion(validQuestion));
        verify(questionMapper, times(1)).insertQuestion(validQuestion);
    }

    @Test
    void addQuestion_MissingBankId_ThrowsException() {
        validQuestion.setBankId(null);
        assertThrows(IllegalArgumentException.class, () -> questionService.addQuestion(validQuestion));
    }

    // updateQuestion 测试
    @Test
    void updateQuestion_ValidInput_ReturnsSuccess() {
        when(questionMapper.updateQuestion(any(Question.class))).thenReturn(1);
        assertEquals(1, questionService.updateQuestion(validQuestion));
        verify(questionMapper, times(1)).updateQuestion(validQuestion);
    }

    @Test
    void updateQuestion_EmptyContent_ThrowsException() {
        validQuestion.setContent("");
        assertThrows(IllegalArgumentException.class, () -> questionService.updateQuestion(validQuestion));
    }

    // deleteQuestion 测试
    @Test
    void deleteQuestion_ValidId_ReturnsSuccess() {
        when(questionMapper.deleteQuestion(anyString())).thenReturn(1);
        assertEquals(1, questionService.deleteQuestion("Q1"));
        verify(questionMapper, times(1)).deleteQuestion("Q1");
    }

    // getQuestionById 测试
    @Test
    void getQuestionById_ExistingId_ReturnsQuestion() {
        when(questionMapper.selectQuestionById("Q1")).thenReturn(validQuestion);
        Question result = questionService.getQuestionById("Q1");
        assertEquals(validQuestion, result);
    }

    // getQuestionsByBankId 测试
    @Test
    void getQuestionsByBankId_ValidId_ReturnsList() {
        List<Question> expected = Collections.singletonList(validQuestion);
        when(questionMapper.selectQuestionsByBankId("B1")).thenReturn(expected);
        List<Question> result = questionService.getQuestionsByBankId("B1");
        assertEquals(expected, result);
    }

    // getQuestionsByCondition 测试
    @Test
    void getQuestionsByCondition_ValidParams_ReturnsList() {
        List<Question> expected = Collections.singletonList(validQuestion);
        when(questionMapper.selectQuestionsByCondition(
                eq("MULTIPLE_CHOICE"),
                eq("HARD"),
                anyList()))
                .thenReturn(expected);

        List<Question> result = questionService.getQuestionsByCondition(
                "MULTIPLE_CHOICE",
                "HARD",
                Arrays.asList("K1", "K2"));

        assertEquals(expected, result);
    }

    // batchAddQuestions 测试
    @Test
    void batchAddQuestions_AllValid_ReturnsSuccess() {
        when(questionMapper.batchInsertQuestions(anyList())).thenReturn(2);
        assertEquals(2, questionService.batchAddQuestions(validQuestions));
    }

    @Test
    void batchAddQuestions_InvalidQuestionInList_ThrowsException() {
        Question invalidQuestion = new Question();
        invalidQuestion.setBankId("B1"); // missing content and type
        List<Question> mixedList = Arrays.asList(validQuestion, invalidQuestion);

        assertThrows(IllegalArgumentException.class,
                () -> questionService.batchAddQuestions(mixedList));
    }

    // getQuestionsByIds 测试
    @Test
    void getQuestionsByIds_ValidIds_ReturnsQuestions() {
        List<String> ids = Arrays.asList("Q1", "Q2");
        when(questionMapper.selectQuestionsByIds(ids)).thenReturn(validQuestions);

        List<Question> result = questionService.getQuestionsByIds(ids);
        assertEquals(2, result.size());
        assertEquals("Q1", result.get(0).getQuestionId());
    }

    @Test
    void getQuestionsByIds_EmptyList_ReturnsEmptyList() {
        List<Question> result = questionService.getQuestionsByIds(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void getQuestionsByIds_NullInput_ReturnsEmptyList() {
        List<Question> result = questionService.getQuestionsByIds(null);
        assertTrue(result.isEmpty());
    }
}
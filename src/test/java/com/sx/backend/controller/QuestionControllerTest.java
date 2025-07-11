package com.sx.backend.controller;

import com.sx.backend.entity.Question;
import com.sx.backend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionControllerTest {

    @InjectMocks
    private QuestionController questionController;

    @Mock
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddQuestion() {
        Question q = new Question();
        when(questionService.addQuestion(q)).thenReturn(1);
        assertEquals(1, questionController.addQuestion(q));
    }

    @Test
    void testUpdateQuestion() {
        Question q = new Question();
        when(questionService.updateQuestion(q)).thenReturn(1);
        assertEquals(1, questionController.updateQuestion(q));
    }

    @Test
    void testDeleteQuestion() {
        when(questionService.deleteQuestion("1")).thenReturn(1);
        assertEquals(1, questionController.deleteQuestion("1"));
    }

    @Test
    void testGetQuestionById() {
        Question q = new Question();
        when(questionService.getQuestionById("1")).thenReturn(q);
        assertEquals(q, questionController.getQuestionById("1"));
    }

    @Test
    void testGetQuestionsByBankId() {
        List<Question> list = Arrays.asList(new Question(), new Question());
        when(questionService.getQuestionsByBankId("2")).thenReturn(list);
        assertEquals(list, questionController.getQuestionsByBankId("2"));
    }

    @Test
    void testGetQuestionsByCondition() {
        List<String> kpIds = Arrays.asList("k1", "k2");
        List<Question> list = Collections.singletonList(new Question());
        when(questionService.getQuestionsByCondition("typeA", "easy", kpIds)).thenReturn(list);
        assertEquals(list, questionController.getQuestionsByCondition("typeA", "easy", kpIds));
    }

    @Test
    void testAddQuestion_exception() {
        Question q = new Question();
        when(questionService.addQuestion(q)).thenThrow(new RuntimeException("error"));
        assertThrows(RuntimeException.class, () -> questionController.addQuestion(q));
    }

    @Test
    void testUpdateQuestion_exception() {
        Question q = new Question();
        when(questionService.updateQuestion(q)).thenThrow(new RuntimeException("error"));
        assertThrows(RuntimeException.class, () -> questionController.updateQuestion(q));
    }

    @Test
    void testDeleteQuestion_exception() {
        when(questionService.deleteQuestion("1")).thenThrow(new RuntimeException("error"));
        assertThrows(RuntimeException.class, () -> questionController.deleteQuestion("1"));
    }

    @Test
    void testGetQuestionById_notFound() {
        when(questionService.getQuestionById("not-exist")).thenReturn(null);
        assertNull(questionController.getQuestionById("not-exist"));
    }
}
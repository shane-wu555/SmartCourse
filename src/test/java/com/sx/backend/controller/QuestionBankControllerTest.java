package com.sx.backend.controller;

import com.sx.backend.entity.QuestionBank;
import com.sx.backend.entity.Question;
import com.sx.backend.service.QuestionBankService;
import com.sx.backend.service.QuestionService;
import com.sx.backend.service.ExcelQuestionImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuestionBankControllerTest {

    @InjectMocks
    private QuestionBankController controller;

    @Mock
    private QuestionBankService questionBankService;
    @Mock
    private QuestionService questionService;
    @Mock
    private ExcelQuestionImportService excelQuestionImportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddQuestionBank() {
        QuestionBank bank = new QuestionBank();
        when(questionBankService.addQuestionBank(bank)).thenReturn(1);
        assertEquals(1, controller.addQuestionBank(bank));
    }

    @Test
    void testDeleteQuestionBank() {
        when(questionBankService.deleteQuestionBank("1")).thenReturn(1);
        assertEquals(1, controller.deleteQuestionBank("1"));
    }

    @Test
    void testUpdateQuestionBank() {
        QuestionBank bank = new QuestionBank();
        when(questionBankService.updateQuestionBank(bank)).thenReturn(1);
        assertEquals(1, controller.updateQuestionBank(bank));
    }

    @Test
    void testGetQuestionBankById() {
        QuestionBank bank = new QuestionBank();
        when(questionBankService.getQuestionBankById("1")).thenReturn(bank);
        assertEquals(bank, controller.getQuestionBankById("1"));
    }

    @Test
    void testGetAllQuestionBanks() {
        List<QuestionBank> banks = Collections.singletonList(new QuestionBank());
        when(questionBankService.getAllQuestionBanks()).thenReturn(banks);
        assertEquals(banks, controller.getAllQuestionBanks());
    }

    @Test
    void testGetQuestionBankInfo_success() {
        QuestionBank bank = new QuestionBank();
        when(questionBankService.getQuestionBankById("1")).thenReturn(bank);
        var resp = controller.getQuestionBankInfo("1");
        assertEquals(bank, resp.getData());
    }

    @Test
    void testGetQuestionBankInfo_notFound() {
        when(questionBankService.getQuestionBankById("2")).thenReturn(null);
        var resp = controller.getQuestionBankInfo("2");
        assertEquals("题库不存在", resp.getMessage());
    }

    @Test
    void testGetQuestionBankInfo_exception() {
        when(questionBankService.getQuestionBankById("3")).thenThrow(new RuntimeException("error"));
        var resp = controller.getQuestionBankInfo("3");
        assertTrue(resp.getMessage().contains("获取题库信息失败"));
    }

    @Test
    void testAddQuestionToBank() {
        Question q = new Question();
        when(questionService.addQuestion(q)).thenReturn(1);
        assertEquals(1, controller.addQuestionToBank("1", q));
        assertEquals("1", q.getBankId());
    }

    @Test
    void testDeleteQuestionFromBank() {
        when(questionService.deleteQuestion("2")).thenReturn(1);
        assertEquals(1, controller.deleteQuestionFromBank("1", "2"));
    }

    @Test
    void testUpdateQuestionInBank() {
        Question q = new Question();
        when(questionService.updateQuestion(q)).thenReturn(1);
        assertEquals(1, controller.updateQuestionInBank("1", q));
        assertEquals("1", q.getBankId());
    }

    @Test
    void testGetQuestionsByBankId() {
        List<Question> questions = Arrays.asList(new Question(), new Question());
        when(questionService.getQuestionsByBankId("1")).thenReturn(questions);
        assertEquals(questions, controller.getQuestionsByBankId("1"));
    }

    @Test
    void testBatchImportQuestions() {
        Question q1 = new Question();
        Question q2 = new Question();
        List<Question> questions = Arrays.asList(q1, q2);
        when(questionService.addQuestion(q1)).thenReturn(1);
        when(questionService.addQuestion(q2)).thenReturn(1);
        assertEquals(2, controller.batchImportQuestions("1", questions));
        assertEquals("1", q1.getBankId());
        assertEquals("1", q2.getBankId());
    }

    @Test
    void testImportQuestionsFromExcel() {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", new byte[0]);
        when(excelQuestionImportService.importQuestionsFromExcel("1", file)).thenReturn(5);
        assertEquals(5, controller.importQuestionsFromExcel("1", file));
    }
}
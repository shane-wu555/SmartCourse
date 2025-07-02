package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.sx.backend.entity.QuestionBank;
import com.sx.backend.mapper.QuestionBankMapper;
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
class QuestionBankServiceImplTest {

    @Mock
    private QuestionBankMapper questionBankMapper;

    @InjectMocks
    private QuestionBankServiceImpl questionBankService;

    private static final String BANK_ID = "bank-001";
    private QuestionBank questionBank;

    @BeforeEach
    void setUp() {
        questionBank = new QuestionBank();
        questionBank.setBankId(BANK_ID);
        questionBank.setName("Java基础题库");
        questionBank.setDescription("Java基础知识题库");
        questionBank.setCourseId("course-001");
    }

    @Test
    void addQuestionBank_Success() {
        // 模拟行为
        when(questionBankMapper.insertQuestionBank(any(QuestionBank.class))).thenReturn(1);

        // 执行方法
        int result = questionBankService.addQuestionBank(questionBank);

        // 验证结果
        assertEquals(1, result);
        verify(questionBankMapper).insertQuestionBank(questionBank);
    }

    @Test
    void addQuestionBank_Failure() {
        // 模拟行为
        when(questionBankMapper.insertQuestionBank(any(QuestionBank.class))).thenReturn(0);

        // 执行方法
        int result = questionBankService.addQuestionBank(questionBank);

        // 验证结果
        assertEquals(0, result);
        verify(questionBankMapper).insertQuestionBank(questionBank);
    }

    @Test
    void deleteQuestionBank_Success() {
        // 模拟行为
        when(questionBankMapper.deleteQuestionBank(BANK_ID)).thenReturn(1);

        // 执行方法
        int result = questionBankService.deleteQuestionBank(BANK_ID);

        // 验证结果
        assertEquals(1, result);
        verify(questionBankMapper).deleteQuestionBank(BANK_ID);
    }

    @Test
    void deleteQuestionBank_Failure() {
        // 模拟行为
        when(questionBankMapper.deleteQuestionBank(BANK_ID)).thenReturn(0);

        // 执行方法
        int result = questionBankService.deleteQuestionBank(BANK_ID);

        // 验证结果
        assertEquals(0, result);
        verify(questionBankMapper).deleteQuestionBank(BANK_ID);
    }

    @Test
    void updateQuestionBank_Success() {
        // 准备更新数据
        QuestionBank updatedBank = new QuestionBank();
        updatedBank.setBankId(BANK_ID);
        updatedBank.setName("更新后的题库名称");

        // 模拟行为
        when(questionBankMapper.updateQuestionBank(updatedBank)).thenReturn(1);

        // 执行方法
        int result = questionBankService.updateQuestionBank(updatedBank);

        // 验证结果
        assertEquals(1, result);
        verify(questionBankMapper).updateQuestionBank(updatedBank);
    }

    @Test
    void updateQuestionBank_Failure() {
        // 准备更新数据
        QuestionBank updatedBank = new QuestionBank();
        updatedBank.setBankId(BANK_ID);
        updatedBank.setName("更新后的题库名称");

        // 模拟行为
        when(questionBankMapper.updateQuestionBank(updatedBank)).thenReturn(0);

        // 执行方法
        int result = questionBankService.updateQuestionBank(updatedBank);

        // 验证结果
        assertEquals(0, result);
        verify(questionBankMapper).updateQuestionBank(updatedBank);
    }

    @Test
    void getQuestionBankById_Exists() {
        // 模拟行为
        when(questionBankMapper.selectQuestionBankById(BANK_ID)).thenReturn(questionBank);

        // 执行方法
        QuestionBank result = questionBankService.getQuestionBankById(BANK_ID);

        // 验证结果
        assertNotNull(result);
        assertEquals(BANK_ID, result.getBankId());
        assertEquals("Java基础题库", result.getName());
        verify(questionBankMapper).selectQuestionBankById(BANK_ID);
    }

    @Test
    void getQuestionBankById_NotExists() {
        // 模拟行为
        when(questionBankMapper.selectQuestionBankById(BANK_ID)).thenReturn(null);

        // 执行方法
        QuestionBank result = questionBankService.getQuestionBankById(BANK_ID);

        // 验证结果
        assertNull(result);
        verify(questionBankMapper).selectQuestionBankById(BANK_ID);
    }

    @Test
    void getAllQuestionBanks_WithData() {
        // 准备数据
        QuestionBank bank1 = new QuestionBank();
        bank1.setBankId("bank-001");

        QuestionBank bank2 = new QuestionBank();
        bank2.setBankId("bank-002");

        List<QuestionBank> banks = Arrays.asList(bank1, bank2);

        // 模拟行为
        when(questionBankMapper.selectAllQuestionBanks()).thenReturn(banks);

        // 执行方法
        List<QuestionBank> result = questionBankService.getAllQuestionBanks();

        // 验证结果
        assertEquals(2, result.size());
        verify(questionBankMapper).selectAllQuestionBanks();
    }

    @Test
    void getAllQuestionBanks_Empty() {
        // 模拟行为
        when(questionBankMapper.selectAllQuestionBanks()).thenReturn(Collections.emptyList());

        // 执行方法
        List<QuestionBank> result = questionBankService.getAllQuestionBanks();

        // 验证结果
        assertTrue(result.isEmpty());
        verify(questionBankMapper).selectAllQuestionBanks();
    }
}
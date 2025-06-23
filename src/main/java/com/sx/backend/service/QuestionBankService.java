package com.sx.backend.service;

import com.sx.backend.entity.QuestionBank;
import java.util.List;

public interface QuestionBankService {
    int addQuestionBank(QuestionBank bank);
    int deleteQuestionBank(String bankId);
    int updateQuestionBank(QuestionBank bank);
    QuestionBank getQuestionBankById(String bankId);
    List<QuestionBank> getAllQuestionBanks();
}

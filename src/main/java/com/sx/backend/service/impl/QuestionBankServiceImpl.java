package com.sx.backend.service.impl;

import com.sx.backend.entity.QuestionBank;
import com.sx.backend.mapper.QuestionBankMapper;
import com.sx.backend.service.QuestionBankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionBankServiceImpl implements QuestionBankService {
    @Autowired
    private QuestionBankMapper questionBankMapper;

    @Override
    public int addQuestionBank(QuestionBank bank) {
        return questionBankMapper.insertQuestionBank(bank);
    }

    @Override
    public int deleteQuestionBank(String bankId) {
        return questionBankMapper.deleteQuestionBank(bankId);
    }

    @Override
    public int updateQuestionBank(QuestionBank bank) {
        return questionBankMapper.updateQuestionBank(bank);
    }

    @Override
    public QuestionBank getQuestionBankById(String bankId) {
        return questionBankMapper.selectQuestionBankById(bankId);
    }

    @Override
    public List<QuestionBank> getAllQuestionBanks() {
        return questionBankMapper.selectAllQuestionBanks();
    }
}

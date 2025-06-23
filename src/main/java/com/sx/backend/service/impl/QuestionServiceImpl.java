package com.sx.backend.service.impl;

import com.sx.backend.entity.Question;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public int addQuestion(Question question) {
        return questionMapper.insertQuestion(question);
    }

    @Override
    public int updateQuestion(Question question) {
        return questionMapper.updateQuestion(question);
    }

    @Override
    public int deleteQuestion(String questionId) {
        return questionMapper.deleteQuestion(questionId);
    }

    @Override
    public Question getQuestionById(String questionId) {
        return questionMapper.selectQuestionById(questionId);
    }

    @Override
    public List<Question> getQuestionsByBankId(String bankId) {
        return questionMapper.selectQuestionsByBankId(bankId);
    }

    @Override
    public List<Question> getQuestionsByCondition(String type, String difficultyLevel, List<String> knowledgePointIds) {
        return questionMapper.selectQuestionsByCondition(type, difficultyLevel, knowledgePointIds);
    }

    @Override
    public int batchAddQuestions(List<Question> questions) {
        // 需在Mapper中实现批量插入
        return questionMapper.batchInsertQuestions(questions);
    }
}

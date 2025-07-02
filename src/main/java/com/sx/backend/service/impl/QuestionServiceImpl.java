package com.sx.backend.service.impl;

import com.sx.backend.entity.Question;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public int addQuestion(Question question) {
        validateQuestion(question);
        return questionMapper.insertQuestion(question);
    }

    @Override
    public int updateQuestion(Question question) {
        validateQuestion(question);
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
    public List<Question> getQuestionsByCondition(String type, String difficultylevel, List<String> knowledgePointIds) {
        return questionMapper.selectQuestionsByCondition(type, difficultylevel, knowledgePointIds);
    }

    @Override
    public int batchAddQuestions(List<Question> questions) {
        // 验证每个题目的必需字段
        for (Question question : questions) {
            validateQuestion(question);
        }
        
        // 需在Mapper中实现批量插入
        return questionMapper.batchInsertQuestions(questions);
    }
    
    @Override
    public List<Question> getQuestionsByIds(List<String> questionIds) {
        System.out.println("DEBUG: QuestionService.getQuestionsByIds called with: " + questionIds);
        
        if (questionIds == null || questionIds.isEmpty()) {
            System.out.println("DEBUG: questionIds is null or empty, returning empty list");
            return new ArrayList<>();
        }
        
        System.out.println("DEBUG: About to call questionMapper.selectQuestionsByIds");
        List<Question> questions = questionMapper.selectQuestionsByIds(questionIds);
        
        System.out.println("DEBUG: questionMapper.selectQuestionsByIds returned: " + (questions != null ? questions.size() : "null") + " questions");
        
        if (questions != null && !questions.isEmpty()) {
            for (int i = 0; i < Math.min(questions.size(), 2); i++) {
                Question q = questions.get(i);
                System.out.println("DEBUG: Question " + i + " - ID: " + q.getQuestionId() + ", Content: " + 
                    (q.getContent() != null ? q.getContent().substring(0, Math.min(50, q.getContent().length())) + "..." : "null"));
            }
        } else {
            System.out.println("WARN: No questions found for IDs: " + questionIds);
        }
        
        return questions;
    }
    
    /**
     * 验证题目的必需字段
     * @param question 题目对象
     * @throws IllegalArgumentException 如果必需字段为空
     */
    private void validateQuestion(Question question) {
        if (question.getBankId() == null || question.getBankId().trim().isEmpty()) {
            throw new IllegalArgumentException("题库ID不能为空");
        }
        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("题干内容不能为空");
        }
        if (question.getType() == null) {
            throw new IllegalArgumentException("题目类型不能为空");
        }
    }
}

package com.sx.backend.service;

import com.sx.backend.entity.Question;
import java.util.List;

public interface QuestionService {
    int addQuestion(Question question);
    int updateQuestion(Question question);
    int deleteQuestion(String questionId);
    Question getQuestionById(String questionId);
    List<Question> getQuestionsByBankId(String bankId);
    List<Question> getQuestionsByCondition(String type, String difficultylevel, List<String> knowledgePointIds);
    int batchAddQuestions(List<Question> questions);
    
    /**
     * 批量根据题目ID获取题目详情
     * @param questionIds 题目ID列表
     * @return 题目详情列表
     */
    List<Question> getQuestionsByIds(List<String> questionIds);
}

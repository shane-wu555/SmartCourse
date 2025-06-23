package com.sx.backend.service;

import com.sx.backend.entity.Question;
import java.util.List;

public interface QuestionService {
    int addQuestion(Question question);
    int updateQuestion(Question question);
    int deleteQuestion(String questionId);
    Question getQuestionById(String questionId);
    List<Question> getQuestionsByBankId(String bankId);
    List<Question> getQuestionsByCondition(String type, String difficultyLevel, List<String> knowledgePointIds);
    int batchAddQuestions(List<Question> questions);
}

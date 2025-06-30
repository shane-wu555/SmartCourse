package com.sx.backend.mapper;

import com.sx.backend.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionMapper {
    int insertQuestion(Question question);
    int updateQuestion(Question question);
    int deleteQuestion(@Param("questionId") String questionId);
    Question selectQuestionById(@Param("questionId") String questionId);
    List<Question> selectQuestionsByBankId(@Param("bankId") String bankId);
    List<Question> selectQuestionsByCondition(@Param("type") String type,
                                              @Param("difficultylevel") String difficultyLevel,
                                              @Param("knowledgePointIds") List<String> knowledgePointIds);
    int batchInsertQuestions(@Param("questions") List<Question> questions);
}

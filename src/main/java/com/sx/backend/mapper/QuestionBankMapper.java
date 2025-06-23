package com.sx.backend.mapper;

import com.sx.backend.entity.QuestionBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionBankMapper {
    int insertQuestionBank(QuestionBank bank);
    int deleteQuestionBank(@Param("bankId") String bankId);
    int updateQuestionBank(QuestionBank bank);
    QuestionBank selectQuestionBankById(@Param("bankId") String bankId);
    List<QuestionBank> selectAllQuestionBanks();
}

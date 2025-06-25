package com.sx.backend.mapper;

import com.sx.backend.entity.AnswerRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AnswerRecordMapper {
    // 根据提交ID查询答案记录
    AnswerRecord findById(String answerRecordId);

    // 更新答案记录
    AnswerRecord update(AnswerRecord answerRecord);

    // 创建新的答案记录
    AnswerRecord create(AnswerRecord answerRecord);

    // 删除答案记录
    void delete(String answerRecordId);
}

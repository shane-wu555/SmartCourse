package com.sx.backend.mapper;

import com.sx.backend.entity.AnswerRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnswerRecordMapper {
    // 根据提交ID查询答案记录
    AnswerRecord findById(String recordId);

    // 更新答案记录
    int update(AnswerRecord answerRecord);

    /*
     * 创建新的答案记录时需要提供提交ID
     * @param answerRecord 答案记录实体
     * @return 新创建的答案记录实体
     */
    int create(AnswerRecord answerRecord);

    // 删除答案记录
    void delete(String answerRecordId);
}

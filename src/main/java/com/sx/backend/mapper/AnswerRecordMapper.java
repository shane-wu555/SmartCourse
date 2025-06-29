package com.sx.backend.mapper;

import com.sx.backend.entity.AnswerRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AnswerRecordMapper {
    // 根据提交ID查询答案记录
    @Select("SELECT * FROM answer_record WHERE submission_id = #{submissionId}")
    AnswerRecord findById(String answerRecordId);

    // 更新答案记录
    @Update("UPDATE answer_record SET answer = #{answer}, " +
            "answer_time = #{answerTime}, is_correct = #{isCorrect} " +
            "WHERE answer_record_id = #{answerRecordId}")
    int update(AnswerRecord answerRecord);

    /*
     * 创建新的答案记录时需要提供提交ID
     * @param answerRecord 答案记录实体
     * @param submissionId 提交ID
     * @return 新创建的答案记录实体
     */
    @Insert("INSERT INTO answer_record (answer_record_id, submission_id, answer, answer_time, is_correct) " +
            "VALUES (#{answerRecordId}, #{submissionId}, #{answer}, #{answerTime}, #{isCorrect})")
    int create(AnswerRecord answerRecord);

    // 删除答案记录
    @Update("DELETE FROM answer_record WHERE answer_record_id = #{answerRecordId}")
    void delete(String answerRecordId);
}

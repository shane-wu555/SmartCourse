package com.sx.backend.mapper;

import com.sx.backend.entity.Submission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubmissionMapper {
    // 根据提交ID查询提交记录
    Submission findById(String submissionId);

    // 更新提交记录
    Submission update(Submission submission);
}

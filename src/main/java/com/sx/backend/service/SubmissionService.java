package com.sx.backend.service;

import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.Submission;

import java.util.List;

public interface SubmissionService {

    /**
     * 提交答题记录
     * @param submissionDTO 提交信息
     * @return 提交结果
     */
    Submission submitAnswerRecords(SubmissionDTO submissionDTO);

    /**
     * 提交文件
     * @param submissionDTO 提交信息
     * @return 提交结果
     */
    Submission submitFiles(SubmissionDTO submissionDTO);
}

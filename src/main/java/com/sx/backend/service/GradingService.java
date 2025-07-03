package com.sx.backend.service;

import com.sx.backend.dto.request.ManualGradingRequest;
import com.sx.backend.entity.ManualGrade;
import com.sx.backend.entity.Submission;
import com.sx.backend.util.Pair;
import com.sx.backend.entity.AnswerRecord;

import java.util.List;
import java.util.Map;

public interface GradingService {
    /**
     * 自动批改学生提交的作业
     * @param submissionId 提交ID
     */
    void autoGradeSubmission(String submissionId);

    /**
     * 手动批改学生提交的作业
     * @param submissionId 提交ID
     * @param questionGrades 每个问题的分数和教师反馈
     * @param feedback 总的教师反馈
     */
    Submission manualGradeSubmission(String submissionId, List<ManualGrade> questionGrades, String feedback);

    /**
     * 获取需要手动批改的问题列表
     * @param answerRecords 答案记录列表
     * @return 需要手动批改的答案记录列表
     */
    List<AnswerRecord> getQuestionForManualGrading(List<AnswerRecord> answerRecords);
}

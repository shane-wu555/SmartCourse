package com.sx.backend.service.impl;

import com.sx.backend.dto.request.ManualGradingRequest;
import com.sx.backend.entity.*;
import com.sx.backend.service.GradeService;
import com.sx.backend.util.Pair;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradingServiceImpl implements GradingService {
    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private GradeService gradeService;


    // 自动批改提交
    @Override
    public void autoGradeSubmission(String submissionId) {
        Submission submission = submissionMapper.findById(submissionId);
        List<AnswerRecord> answerRecords = new ArrayList<AnswerRecord>();
        for (String recordId : submission.getAnswerRecords()) {
            AnswerRecord record = answerRecordMapper.findById(recordId);
            answerRecords.add(record);
        }

        // 自动批改
        Float autoScore = 0.0f;
        for (AnswerRecord record : answerRecords) {
            Question question = questionMapper.selectQuestionById(record.getQuestionId());
            if (question.isAutoGradable()) {
                record.setObtainedScore(question.autoGrade(record.getAnswers()));
                record.setAutoGraded(true);
                autoScore += record.getObtainedScore();
            }
            else {
                record.setObtainedScore(0.0f);
                record.setAutoGraded(false);
            }

            answerRecordMapper.update(record);
        }

        submission.setAutoGrade(autoScore);
        submission.setFinalGrade(autoScore);
        submission.setStatus(SubmissionStatus.AUTO_GRADED);
        submission.setGradeTime(LocalDateTime.now());

        gradeService.updateTaskGrade(submission);

        submissionMapper.update(submission);
    }

    @Override
    public Submission manualGradeSubmission(String submissionId, List<ManualGrade> questionGrades, String feedback) {
        Submission submission = submissionMapper.findById(submissionId);
        List<AnswerRecord> answerRecords = new ArrayList<>();
        for (String recordId : submission.getAnswerRecords()) {
            AnswerRecord record = answerRecordMapper.findById(recordId);
            answerRecords.add(record);
        }
        List<AnswerRecord> manualRecords = getQuestionForManualGrading(answerRecords);
        float grade = submission.getAutoGrade() != null ? submission.getAutoGrade() : 0.0f;

        for (AnswerRecord record : manualRecords) {
            ManualGrade manualGrade = questionGrades.stream()
                    .filter(qg -> qg.getRecordId().equals(record.getRecordId()))
                    .findFirst()
                    .orElse(null);
            if (manualGrade != null) {
                // 正常赋值
                Float score = manualGrade.getScore();
                String questionFeedback = manualGrade.getFeedback();
                if (score != null) {
                    manualGrading(record, score, questionFeedback);
                    grade += score;
                }
            }
            else {
                throw new IllegalArgumentException("提交中缺少问题 " + record.getRecordId() + " 的评分信息");
            }
        }

        submission.setFinalGrade(grade);
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setFeedback(feedback);
        submission.setGradeTime(LocalDateTime.now());

        gradeService.updateTaskGrade(submission);

        return submission;
    }

    @Override
    // 获取需要手动批改的题目
    public List<AnswerRecord> getQuestionForManualGrading(List<AnswerRecord> answerRecords) {
        return answerRecords.stream()
                .filter(record -> !record.isAutoGraded())
                .collect(Collectors.toList());
    }

    // 手动批改提交
    public void manualGrading(AnswerRecord answerRecord, Float grade, String feedback) {
        // 实现手动批改逻辑
        Question question = questionMapper.selectQuestionById(answerRecord.getQuestionId());
        if (question.isAutoGradable()) {
            throw new IllegalArgumentException("此题型应该自动评分，不能手动评分");
        }
        answerRecord.setObtainedScore(grade);
        answerRecord.setTeacherFeedback(feedback);
        answerRecordMapper.update(answerRecord);
    }
}

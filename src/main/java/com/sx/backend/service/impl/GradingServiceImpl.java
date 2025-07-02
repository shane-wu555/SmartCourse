package com.sx.backend.service.impl;

import com.sx.backend.util.Pair;
import com.sx.backend.entity.AnswerRecord;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.SubmissionStatus;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        }

        submission.setAutoGrade(autoScore);
        submission.setFinalGrade(autoScore);
        submission.setStatus(SubmissionStatus.AUTO_GRADED);

        submissionMapper.update(submission);
    }

    @Override
    public void manualGradeSubmission(String submissionId, Map<String, Pair<Float, String>> questionGrades, String feedback) {
        Submission submission = submissionMapper.findById(submissionId);
        List<AnswerRecord> answerRecords = new ArrayList<>();
        for (String recordId : submission.getAnswerRecords()) {
            AnswerRecord record = answerRecordMapper.findById(recordId);
            answerRecords.add(record);
        }
        List<AnswerRecord> manualRecords = getQuestionForManualGrading(answerRecords);
        Float grade = submission.getFinalGrade();

        for (AnswerRecord record : manualRecords) {
            Pair<Float, String> gradePair = questionGrades.get(record.getRecordId());
            if (gradePair != null) {
                // 正常赋值
                Float score = gradePair.first;
                String questionFeedback = gradePair.second;
                if (score != null) {
                    manualGrade(record, score, questionFeedback);
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
    }

    @Override
    // 获取需要手动批改的题目
    public List<AnswerRecord> getQuestionForManualGrading(List<AnswerRecord> answerRecords) {
        return answerRecords.stream()
                .filter(record -> !record.isAutoGraded())
                .collect(Collectors.toList());
    }

    // 手动批改提交
    public void manualGrade(AnswerRecord answerRecord, Float grade, String feedback) {
        // 实现手动批改逻辑
        Question question = questionMapper.selectQuestionById(answerRecord.getQuestionId());
        if (question.isAutoGradable()) {
            throw new IllegalArgumentException("此题型应该自动评分，不能手动评分");
        }
        answerRecord.setObtainedScore(grade);
        answerRecord.setTeacherFeedback(feedback);
    }
}

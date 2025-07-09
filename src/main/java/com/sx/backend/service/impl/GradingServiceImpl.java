package com.sx.backend.service.impl;

import com.sx.backend.entity.*;
import com.sx.backend.service.GradeService;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.QuestionMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
            if (question != null && question.isAutoGradable()) {
                // 可以自动批改的题目
                Float questionScore = question.autoGrade(record.getAnswers());
                record.setObtainedScore(questionScore);
                record.setAutoGraded(true);
                autoScore += questionScore;
                
                System.out.println("DEBUG: 自动批改题目 " + record.getQuestionId() + 
                                 " 类型: " + question.getType() + 
                                 " 学生答案: " + record.getAnswers() + 
                                 " 正确答案: " + question.getAnswer() + 
                                 " 选项: " + question.getOptions() + 
                                 " 得分: " + questionScore + 
                                 " 题目分值: " + question.getScore() + 
                                 " 当前总分: " + autoScore);
            }
            else {
                // 不能自动批改的题目（如简答题、编程题）或者题目不存在
                record.setObtainedScore(0.0f);
                record.setAutoGraded(false);
                System.out.println("DEBUG: 跳过手动批改题目 " + record.getQuestionId() + 
                                 " 类型: " + (question != null ? question.getType() : "null"));
            }

            answerRecordMapper.update(record);
        }

        submission.setAutoGrade(autoScore);
        submission.setFinalGrade(autoScore);
        submission.setStatus(SubmissionStatus.AUTO_GRADED);
        submission.setGradeTime(LocalDateTime.now());

        System.out.println("DEBUG: 自动批改完成，总分: " + autoScore);
        System.out.println("DEBUG: 提交状态: " + submission.getStatus());
        System.out.println("DEBUG: 最终分数: " + submission.getFinalGrade());

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

        System.out.println("DEBUG: 手动批改开始，自动批改基础分数: " + grade);

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
                    System.out.println("DEBUG: 手动批改题目 " + record.getQuestionId() + 
                                     " 得分: " + score + 
                                     " 当前总分: " + grade);
                }
            }
            else {
                throw new IllegalArgumentException("提交中缺少问题 " + record.getRecordId() + " 的评分信息");
            }
        }

        System.out.println("DEBUG: 手动批改完成，最终总分: " + grade);

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
                .filter(record -> {
                    // 检查题目类型，确保只有真正需要手动批改的题目才被返回
                    Question question = questionMapper.selectQuestionById(record.getQuestionId());
                    if (question == null) {
                        return false; // 如果找不到题目，不需要手动批改
                    }
                    
                    // 只有非自动批改类型的题目才需要手动批改
                    // 简答题(SHORT_ANSWER)、编程题(PROGRAMMING)和填空题(FILL_BLANK)需要手动批改
                    boolean isManualGradableType = question.getType() == QuestionType.SHORT_ANSWER || 
                                                  question.getType() == QuestionType.PROGRAMMING ||
                                                  question.getType() == QuestionType.FILL_BLANK;
                    
                    // 对于需要手动批改的题目类型，直接返回true，不管autoGraded状态
                    // 因为这些题目本身就不应该被自动批改
                    return isManualGradableType;
                })
                .collect(Collectors.toList());
    }

    // 手动批改提交
    public void manualGrading(AnswerRecord answerRecord, Float grade, String feedback) {
        // 实现手动批改逻辑
        Question question = questionMapper.selectQuestionById(answerRecord.getQuestionId());
        if (question == null) {
            throw new IllegalArgumentException("找不到题目ID为 " + answerRecord.getQuestionId() + " 的题目");
        }
        
        // 检查题目类型，确保只有真正需要手动批改的题目才能被手动批改
        boolean isManualGradableType = question.getType() == QuestionType.SHORT_ANSWER || 
                                      question.getType() == QuestionType.PROGRAMMING ||
                                      question.getType() == QuestionType.FILL_BLANK;
        
        if (!isManualGradableType) {
            throw new IllegalArgumentException("题目类型为 " + question.getType() + " 的题目应该自动评分，不能手动评分");
        }
        
        answerRecord.setObtainedScore(grade);
        answerRecord.setTeacherFeedback(feedback);
        answerRecord.setAutoGraded(false); // 手动批改的题目标记为非自动批改
        answerRecordMapper.update(answerRecord);
    }
}

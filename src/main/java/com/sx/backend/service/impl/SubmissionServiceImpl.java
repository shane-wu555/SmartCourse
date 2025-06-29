package com.sx.backend.service.impl;

import com.sx.backend.dto.AnswerRecordDTO;
import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.AnswerRecord;
import com.sx.backend.entity.Question;
import com.sx.backend.entity.Submission;
import com.sx.backend.mapper.*;
import com.sx.backend.service.GradingService;
import com.sx.backend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubmissionServiceImpl implements SubmissionService {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private AnswerRecordMapper answerRecordMapper;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private GradingService gradingService;

    @Override
    public Submission submitAnswerRecords(SubmissionDTO submissionDTO) {
        // 实现提交答题记录的逻辑
        // 1. 验证任务ID和学生ID
        if (taskMapper.getById(submissionDTO.getTaskId()) == null) {
            throw new IllegalArgumentException("Invalid task ID: " + submissionDTO.getTaskId());
        }
        if (studentMapper.selectById(submissionDTO.getStudentId()) == null) {
            throw new IllegalArgumentException("Invalid student ID: " + submissionDTO.getStudentId());
        }

        // 2. 创建提交记录
        Submission submission = new Submission();
        submission.setTaskId(submissionDTO.getTaskId());
        submission.setStudentId(submissionDTO.getStudentId());
        submission.setSubmitTime(submissionDTO.getSubmitTime());

        List<AnswerRecordDTO> answerRecordDTO = submissionDTO.getAnswerRecordDTO();
        if (answerRecordDTO == null) {
            throw new IllegalArgumentException("Invalid answer record DTO");
        }

        List<String> answerRecordIds = new ArrayList<>();
        // 3. 创建答题记录
        for (AnswerRecordDTO recordDTO : answerRecordDTO) {
            if (recordDTO.getQuestionId() == null || recordDTO.getStudentAnswers() == null) {
                throw new IllegalArgumentException("Invalid answer record data");
            }
            AnswerRecord answerRecord = new AnswerRecord();
            answerRecord.setRecordId(UUID.randomUUID().toString());
            answerRecord.setQuestionId(recordDTO.getQuestionId());
            answerRecord.setStudentAnswers(recordDTO.getStudentAnswers());
            Question question = questionMapper.selectQuestionById(recordDTO.getQuestionId());
            if (question == null) {
                throw new IllegalArgumentException("Invalid question ID: " + recordDTO.getQuestionId());
            }
            answerRecord.setObtainedScore(question.getScore());
            answerRecordIds.add(answerRecord.getRecordId());
            answerRecordMapper.create(answerRecord);
        }

        submission.setAnswerRecords(answerRecordIds);

        gradingService.autoGradeSubmission(submission.getSubmissionId());

        return submissionMapper.create(submission) != 0 ? submission : null; // 返回提交结果
    }

    @Override
    public Submission submitFiles(SubmissionDTO submissionDTO) {
        // 实现提交文件的逻辑
        if (taskMapper.getById(submissionDTO.getTaskId()) == null) {
            throw new IllegalArgumentException("Invalid task ID: " + submissionDTO.getTaskId());
        }
        if (studentMapper.selectById(submissionDTO.getStudentId()) == null) {
            throw new IllegalArgumentException("Invalid student ID: " + submissionDTO.getStudentId());
        }

        Submission submission = new Submission();
        submission.setTaskId(submissionDTO.getTaskId());
        submission.setStudentId(submissionDTO.getStudentId());
        submission.setSubmitTime(submissionDTO.getSubmitTime());

        submission.setFiles(submissionDTO.getFileId()); // 设置文件列表

        return submissionMapper.create(submission) != 0 ? submission : null; // 返回提交结果
    }
}


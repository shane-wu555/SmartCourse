package com.sx.backend.controller;

import com.sx.backend.dto.request.ManualGradingRequest;
import com.sx.backend.entity.AnswerRecord;
import com.sx.backend.entity.Submission;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradingService;
import com.sx.backend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/grading")
public class GradingController {

    @Autowired
    private AnswerRecordMapper answerRecordMapper;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private GradingService gradingService;

    // 获取需要手动批改的问题
    @GetMapping("/manual-questions/{submissionId}")
    public ResponseEntity<List<AnswerRecord>> getManualQuestions(
            @PathVariable String submissionId) {

        Submission submission = submissionMapper.findById(submissionId);
        List<AnswerRecord> answerRecords = new ArrayList<>();
        for (String recordId : submission.getAnswerRecords()) {
            AnswerRecord record = answerRecordMapper.findById(recordId);
            if (record != null) {
                answerRecords.add(record);
            }
        }

        List<AnswerRecord> manualQuestions = gradingService.getQuestionForManualGrading(answerRecords);
        return ResponseEntity.ok(manualQuestions);
    }

    // 提交批改结果
    @PostMapping("/grade-manual/{submissionId}")
    public ResponseEntity<Submission> submitManualGrading(
            @PathVariable String submissionId,
            @RequestBody ManualGradingRequest request) {

        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        // 批改简答题
        gradingService.manualGradeSubmission(submissionId, request.getQuestionGrades(), request.getFeedback());

        // 更新提交状态和批改时间
        Submission updated = submissionMapper.update(submission);

        return ResponseEntity.ok(updated);
    }
}

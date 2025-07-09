package com.sx.backend.controller;

import com.sx.backend.dto.request.ManualGradingRequest;
import com.sx.backend.entity.AnswerRecord;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.SubmissionStatus;
import com.sx.backend.mapper.AnswerRecordMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.GradeService;
import com.sx.backend.service.GradingService;
import com.sx.backend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @Autowired
    private GradeService gradeService;

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
    @PutMapping("/grade-manual/{submissionId}")
    public ResponseEntity<Submission> submitManualGrading(
            @PathVariable String submissionId,
            @RequestBody ManualGradingRequest request) {

        Submission submission = submissionMapper.findById(submissionId);
        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        // 批改简答题
        submission = gradingService.manualGradeSubmission(submissionId, request.getQuestionGrades(), request.getFeedback());

        // 更新提交状态和批改时间
        int updated = submissionMapper.update(submission);

        if (updated == 0) {
            return ResponseEntity.status(500).body(null);
        }
        return ResponseEntity.ok(submission);
    }

    // 自动评分接口
    @PostMapping("/auto/{submissionId}")
    public ResponseEntity<Submission> autoGradeSubmission(
            @PathVariable String submissionId) {
        try {
            gradingService.autoGradeSubmission(submissionId);
            Submission submission = submissionMapper.findById(submissionId);
            
            if (submission == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(submission);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // 获取所有待批改的提交记录
    @GetMapping("/get-works/{taskId}")
    public ResponseEntity<List<Submission>> getWorksForGrading(
            @PathVariable String taskId
    ) {
        // 获取所有待批改的提交记录
        List<Submission> submissions = submissionMapper.findByTaskId(taskId);
        if (submissions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<Submission> ungradedSubmissions = submissions.stream()
                .filter(submission -> submission.getStatus() == SubmissionStatus.SUBMITTED)
                .toList();
        if (ungradedSubmissions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(ungradedSubmissions);
    }

    // 提交批改结果
    @PutMapping("/grade-works")
    public ResponseEntity<Submission> submitWorkGrading(
            @RequestParam("submission_id") String submissionId,
            @RequestParam("grade") float grade,
            @RequestParam(value = "feedback", required = false) String feedback ) {
        Submission submission = submissionMapper.findById(submissionId);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        // 更新提交的成绩和反馈
        submission.setFinalGrade(grade);
        submission.setFeedback(feedback);
        submission.setGradeTime(LocalDateTime.now());
        submission.setStatus(SubmissionStatus.GRADED);

        int submitted = submissionMapper.update(submission);
        gradeService.updateTaskGrade(submission);

        return submitted == 0 ? ResponseEntity.status(500).body(null) : ResponseEntity.ok(submission);
    }
}

package com.sx.backend.controller;

import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.Submission;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.service.SubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private SubmissionService submitService;

    @GetMapping("/getSubmissions/{taskId}")
    public List<Submission> getSubmissions(String taskId) {
        // 调用服务层获取指定课程的所有提交记录
        return submissionMapper.findByTaskId(taskId);
    }

    // 更改提交记录状态为已完成
    @PutMapping("/complete/{submissionId}")
    public int completeSubmission(@PathVariable String submissionId) {
        // 返回受影响的行数
        return submissionMapper.updateCompletedToTrue(submissionId);
    }

    // 提交新的提交记录
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<Submission>> submitSubmission(@RequestBody SubmissionDTO submissionDTO) {
        if (submissionDTO == null) {
            throw new IllegalArgumentException("Submission data cannot be null");
        }
        if (submissionDTO.getFileId() != null) {
            Submission submitted = submitService.submitFiles(submissionDTO);
            return ResponseEntity.ok(ApiResponse.success("提交成功", submitted));
        }
        else if (submissionDTO.getAnswerRecordDTO() != null) {
            Submission submitted = submitService.submitAnswerRecords(submissionDTO);
            return ResponseEntity.ok(ApiResponse.success("提交成功", submitted));
        } else {
            throw new IllegalArgumentException("Submission must contain either fileId or answerRecordId");
        }
    }

    // 更新提交记录
    @PutMapping("/update")
    public int updateSubmission(@RequestBody Submission submission) {
        // 更新提交记录
        return submissionMapper.update(submission);
    }
}

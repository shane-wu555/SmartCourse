package com.sx.backend.controller;

import com.sx.backend.entity.Submission;
import com.sx.backend.mapper.SubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    @Autowired
    private SubmissionMapper submissionMapper;

    @GetMapping("/getSubmissions/{taskId}")
    public List<Submission> getSubmissions(String taskId) {
        // 调用服务层获取指定课程的所有提交记录
        return submissionMapper.findByTaskId(taskId);
    }

    @PutMapping("/complete/{submissionId}")
    public int completeSubmission(@PathVariable String submissionId) {
        // 返回受影响的行数
        return submissionMapper.updateCompletedToTrue(submissionId);
    }
}

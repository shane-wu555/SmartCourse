package com.sx.backend.controller;

import com.sx.backend.dto.AnswerRecordDTO;
import com.sx.backend.dto.SubmissionDTO;
import com.sx.backend.entity.Submission;
import com.sx.backend.entity.SubmissionStatus;
import com.sx.backend.entity.TaskType;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.SubmissionMapper;
import com.sx.backend.mapper.TaskMapper;
import com.sx.backend.service.SubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    @Value("${file.storage.location}")
    private String storageLocation;

    private final HttpServletRequest request;

    @Autowired
    private SubmissionMapper submissionMapper;

    @Autowired
    private SubmissionService submitService;

    @Autowired
    private TaskMapper taskMapper;

    private String getCurrentStudentId() {
        // 从请求属性中获取拦截器设置的userId
        return (String) request.getAttribute("userId");
    }

    @GetMapping("/getSubmissions/{taskId}")
    public List<Submission> getSubmissions(@PathVariable String taskId) {
        // 调用服务层获取指定课程的所有提交记录
        return submissionMapper.findByTaskId(taskId);
    }

    // 更改提交记录状态为已完成
    @PutMapping("/complete/{taskId}")
    public ResponseEntity<ApiResponse<Submission>> completeSubmission(@PathVariable String taskId) {
        // 返回受影响的行数
        String studentId = getCurrentStudentId();
        String submissionId;
        if (taskId == null || studentId == null) {
            throw new IllegalArgumentException("TaskId或StudentID不能为空");
        }
        Submission submission = submissionMapper.findByTaskIdAndStudentId(taskId, studentId);
        if (submission == null) {
            submission = new Submission();
            submission.setTaskId(taskId);
            submission.setStudentId(studentId);
            submission.setCompleted(true);
            submission.setSubmitTime(java.time.LocalDateTime.now());
            submissionId = UUID.randomUUID().toString();
            submission.setSubmissionId(submissionId);
            submission.setFinalGrade(taskMapper.getById(taskId).getMaxScore());
            submission.setStatus(SubmissionStatus.GRADED);
            submissionMapper.create(submission);
        }
        else {
            submission.setCompleted(true);
            submission.setFinalGrade(taskMapper.getById(taskId).getMaxScore());
            submission.setStatus(SubmissionStatus.GRADED);
            submissionMapper.update(submission);
        }

        return ResponseEntity.ok(
                new ApiResponse<>(200, "提交已完成", submission)
        );
    }

    // 提交新的提交记录
    @PostMapping("/submit/files")
    public ResponseEntity<ApiResponse<Submission>> submitFiles(
            @RequestParam("task_id") String taskId,
            @RequestParam("files") List<MultipartFile> files){
        String studentId = getCurrentStudentId();

        if (taskId == null || studentId == null) {
            throw new IllegalArgumentException("Submission data cannot be null");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        List<String> filePaths = new ArrayList<>();
        for (MultipartFile file : files) {
            validateFileSize(file);

            String originalFilename = file.getOriginalFilename();
            String safeFilename = generateSafeFilename(originalFilename);
            String uniqueFilename = UUID.randomUUID() + "_" + safeFilename;

            Path courseDir = Paths.get(storageLocation, "submissions", taskId, studentId);
            if (!Files.exists(courseDir)) {
                try {
                    Files.createDirectories(courseDir);
                } catch (IOException e) {
                    return ResponseEntity.status(500)
                            .body(new ApiResponse<>(400, "创建目录失败: " + e.getMessage(), null));
                }
            }

            Path filePath = courseDir.resolve(uniqueFilename);
            try {
                Files.copy(file.getInputStream(), filePath);
            } catch (IOException e) {
                return ResponseEntity.status(500)
                        .body(new ApiResponse<>(400, "文件保存失败： " + e.getMessage(), null));
            }

            filePaths.add("/submissions/" + taskId + "/" + studentId + "/" + uniqueFilename);
        }

        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setTaskId(taskId);
        submissionDTO.setStudentId(studentId);
        submissionDTO.setSubmitTime(java.time.LocalDateTime.now());
        submissionDTO.setFileId(filePaths);

        return ResponseEntity.ok(
                new ApiResponse<>(201, "提交成功", submitService.submitFiles(submissionDTO))
        );
    }

    @PostMapping("/submit/answers")
    public ResponseEntity<ApiResponse<Submission>> submitAnswers(
            @RequestBody SubmissionDTO submissionDTO
    ) {
        String studentId = getCurrentStudentId();
        String taskId = submissionDTO.getTaskId();
        List<AnswerRecordDTO> answerRecords = submissionDTO.getAnswerRecordDTO();
        if (taskId == null || studentId == null) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(401, "TaskId或StudentID为空 ", null));
        }
        if (taskMapper.getById(taskId) == null) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(402, "Task中不存在该任务 " + taskId, null));
        }
        if (taskMapper.getById(taskId).getType() != TaskType.EXAM_QUIZ) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(400, "任务类型不是试卷答题 ", null));
        }
        if (answerRecords == null || answerRecords.isEmpty()) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(403, "AnswerRecords不能为空 ", null));
        }

        submissionDTO.setStudentId(studentId);
        submissionDTO.setSubmitTime(java.time.LocalDateTime.now());
        submissionDTO.setAnswerRecordDTO(answerRecords);

        Submission submission = submitService.submitAnswerRecords(submissionDTO);
        if (submission == null) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(500, "提交失败", null));
        }

        return ResponseEntity.ok(
                new ApiResponse<>(201, "提交成功", submission)
        );
    }

    // 更新提交记录
    @PutMapping("/update")
    public int updateSubmission(@RequestBody Submission submission) {
        // 更新提交记录
        return submissionMapper.update(submission);
    }

    private void validateFileSize(MultipartFile file) {
        long maxSize = 100 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过100MB限制");
        }
    }

    private String generateSafeFilename(String filename) {
        String safeName = filename.replaceAll(".*[/\\\\]", "");
        safeName = safeName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        safeName = safeName.replaceAll("\\.\\.", "_");
        return safeName;
    }
}

package com.sx.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionDTO {
    private String taskId;
    private String studentId;
    private LocalDateTime submitTime;
    private List<String> fileId; // 上传的文件url
    private List<AnswerRecordDTO> answerRecordDTO;
}

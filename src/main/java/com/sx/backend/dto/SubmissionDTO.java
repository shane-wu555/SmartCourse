package com.sx.backend.dto;

import com.sx.backend.entity.FileMeta;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionDTO {
    private String taskId;
    private String studentId;
    private LocalDateTime submitTime;
    private List<FileMeta> fileId; // 上传的文件
    private List<AnswerRecordDTO> answerRecordDTO;
}

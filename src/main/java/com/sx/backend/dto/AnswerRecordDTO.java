package com.sx.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerRecordDTO {
    private String questionId;
    private List<String> answers;
    private Float obtainedScore;
}

package com.sx.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnswerRecordDTO {
    private String questionId;
    private List<String> studentAnswers;
    private Float obtainedScore;
}

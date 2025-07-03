package com.sx.backend.entity;

public class ManualGrade {
    private String recordId;
    private Float score; // 分数
    private String feedback;

    public ManualGrade(String recordId, Float score, String feedback) {
        this.recordId = recordId;
        this.score = score;
        this.feedback = feedback;
    }

    public ManualGrade() {
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

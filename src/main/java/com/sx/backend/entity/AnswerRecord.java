package com.sx.backend.entity;

import java.util.List;

public class AnswerRecord {
    private String recordId;
    private String submissionId; // 提交ID
    private String questionId;
    private List<String> answers;
    private Float obtainedScore;
    private boolean autoGraded;
    private String teacherFeedback;

    public AnswerRecord(String recordId, String question, List<String> answers, Float obtainedScore,
                        boolean autoGraded, String teacherFeedback) {
        this.recordId = recordId;
        this.questionId = question;
        this.answers = answers;
        this.obtainedScore = obtainedScore;
        this.autoGraded = autoGraded;
        this.teacherFeedback = teacherFeedback;
    }

    public AnswerRecord() {
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public Float getObtainedScore() {
        return obtainedScore;
    }

    public void setObtainedScore(Float obtainedScore) {
        this.obtainedScore = obtainedScore;
    }

    public boolean isAutoGraded() {
        return autoGraded;
    }

    public void setAutoGraded(boolean autoGraded) {
        this.autoGraded = autoGraded;
    }

    public String getTeacherFeedback() {
        return teacherFeedback;
    }

    public void setTeacherFeedback(String teacherFeedback) {
        this.teacherFeedback = teacherFeedback;
    }
}


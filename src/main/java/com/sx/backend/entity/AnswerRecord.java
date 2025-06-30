package com.sx.backend.entity;

import java.util.List;

public class AnswerRecord {
    private String recordId;
    private String questionId;
    private List<String> studentAnswers;
    private Float obtainedScore;
    private boolean autoGraded;
    private String teacherFeedback;

    public AnswerRecord(String recordId, String question, List<String> studentAnswers, Float obtainedScore,
                        boolean autoGraded, String teacherFeedback) {
        this.recordId = recordId;
        this.questionId = question;
        this.studentAnswers = studentAnswers;
        this.obtainedScore = obtainedScore;
        this.autoGraded = autoGraded;
        this.teacherFeedback = teacherFeedback;
    }

    public AnswerRecord() {
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

    public List<String> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(List<String> studentAnswers) {
        this.studentAnswers = studentAnswers;
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

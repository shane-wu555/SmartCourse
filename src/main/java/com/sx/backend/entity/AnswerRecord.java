package com.sx.backend.entity;

import java.util.Set;

public class AnswerRecord {
    private String recordId;
    private Question question;
    private Set<String> studentAnswers;
    private Float obtainedScore;
    private boolean autoGraded;
    private String teacherFeedback;

    public AnswerRecord(String recordId, Question question, Set<String> studentAnswers, Float obtainedScore,
                        boolean autoGraded, String teacherFeedback) {
        this.recordId = recordId;
        this.question = question;
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

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Set<String> getStudentAnswers() {
        return studentAnswers;
    }

    public void setStudentAnswers(Set<String> studentAnswers) {
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

    // 自动评分方法
    public void autoGrade() {
        if (question.isAutoGradable()) {
            this.obtainedScore = question.autoGrade(studentAnswers);
            this.autoGraded = true;
        } else {
            this.autoGraded = false;
            this.obtainedScore = 0.0f; // 如果不能自动评分，默认分数为0
        }
    }

    // 教师评分方法
    public void manualGrade(Float score, String feedback) {
        if (question.isAutoGradable()) {
            throw new IllegalArgumentException("此题型应该自动评分，不能手动评分");
        }
        this.obtainedScore = score;
        this.teacherFeedback = feedback;
    }
}

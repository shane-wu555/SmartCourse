package com.sx.backend.entity;

import java.time.LocalDateTime;

public class TaskGrade {
    private String taskGradeId;
    private String studentId;
    private String taskId;
    private Float score;
    private LocalDateTime submissionTime;
    private String feedback;

    public TaskGrade(String taskGradeId, String studentId, String taskId, Float score, LocalDateTime submissionTime, String feedback) {
        this.taskGradeId = taskGradeId;
        this.studentId = studentId;
        this.taskId = taskId;
        this.score = score;
        this.submissionTime = submissionTime;
        this.feedback = feedback;
    }

    public TaskGrade() {
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getTaskGradeId() {
        return taskGradeId;
    }

    public void setTaskGradeId(String taskGradeId) {
        this.taskGradeId = taskGradeId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public LocalDateTime getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(LocalDateTime submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

package com.sx.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public class TaskGrade {
    private String taskGradeId;
    private Student student;
    private Task task;
    private Float score;
    private Float completionRate;
    private LocalDateTime submissionTime;
    private String feedback;
    private JsonNode detailedAnalysis;

    public TaskGrade(String taskGradeId, Student student, Task task, Float score, Float completionRate, LocalDateTime submissionTime, String feedback, JsonNode detailedAnalysis) {
        this.taskGradeId = taskGradeId;
        this.student = student;
        this.task = task;
        this.score = score;
        this.completionRate = completionRate;
        this.submissionTime = submissionTime;
        this.feedback = feedback;
        this.detailedAnalysis = detailedAnalysis;
    }

    public TaskGrade() {
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getTaskGradeId() {
        return taskGradeId;
    }

    public void setTaskGradeId(String taskGradeId) {
        this.taskGradeId = taskGradeId;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Float getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Float completionRate) {
        this.completionRate = completionRate;
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

    public JsonNode getDetailedAnalysis() {
        return detailedAnalysis;
    }

    public void setDetailedAnalysis(JsonNode detailedAnalysis) {
        this.detailedAnalysis = detailedAnalysis;
    }
}

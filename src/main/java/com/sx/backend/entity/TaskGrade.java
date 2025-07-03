package com.sx.backend.entity;

import java.time.LocalDateTime;

public class TaskGrade {
    private String taskGradeId;
    private String studentId;
    private String courseId;
    private String taskId;
    private Float score;
    private LocalDateTime gradedTime;
    private String feedback;

    public TaskGrade(String taskGradeId, String studentId, String taskId, Float score, LocalDateTime gradedTime, String feedback) {
        this.taskGradeId = taskGradeId;
        this.studentId = studentId;
        this.taskId = taskId;
        this.score = score;
        this.gradedTime = gradedTime;
        this.feedback = feedback;
    }

    public TaskGrade() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public LocalDateTime getGradedTime() {
        return gradedTime;
    }

    public void setGradedTime(LocalDateTime gradedTime) {
        this.gradedTime = gradedTime;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

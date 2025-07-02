package com.sx.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

public class TestPaper {
    private String paperId;
    private String taskId;
    private String title;
    private String courseId; // 课程ID
    private List<Question> questions; // 修改为存储完整题目对象
    private Float totalScore;
    private Integer timeLimit; // 以分钟为单位
    private PaperGenerationMethod generationMethod;
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间

    public TestPaper(String paperId, String taskId, String title, List<Question> questions, Float totalScore, Integer timeLimit, PaperGenerationMethod generationMethod) {
        this.paperId = paperId;
        this.taskId = taskId;
        this.title = title;
        this.questions = questions;
        this.totalScore = totalScore;
        this.timeLimit = timeLimit;
        this.generationMethod = generationMethod;
    }

    public TestPaper() {
    }

    public String getPaperId() {
        return paperId;
    }

    public void setPaperId(String paperId) {
        this.paperId = paperId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Float getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Float totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public PaperGenerationMethod getGenerationMethod() {
        return generationMethod;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    // 设置组卷方式
    public void setGenerationMethod(PaperGenerationMethod generationMethod) {
        this.generationMethod = generationMethod;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

package com.sx.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Task {
    private String taskId;
    private String courseId;
    private String title;
    private String description;
    private TaskType type;
    private String testPaperId;
    private LocalDateTime deadline;
    private Float maxScore;
    private List<Resource> resources;
    private List<KnowledgePoint> knowledgePoints;
    private List<Submission> submissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Task() {}

    public Task(String courseId, String title, String description, TaskType type, LocalDateTime deadline, Float maxScore) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.deadline = deadline;
        this.maxScore = maxScore;
    }
  
    public Task(String taskId, String courseId, String title, TaskType type, LocalDateTime deadline, Float maxScore,
                List<Resource> resources, List<Submission> submissions) {
        this.taskId = taskId;
        this.courseId = courseId;
        this.title = title;
        this.type = type;
        this.deadline = deadline;
        this.maxScore = maxScore;
        this.resources = resources;
        this.submissions = submissions;
    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
  
    public String getTestPaperId() {
        return testPaperId;
    }

    public void setTestPaperId(String testPaper) {
        this.testPaperId = testPaper;
    }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }

    public Float getMaxScore() { return maxScore; }
    public void setMaxScore(Float maxScore) { this.maxScore = maxScore; }

    public List<Resource> getResources() { return resources; }
    public void setResources(List<Resource> resources) { this.resources = resources; }

    public List<KnowledgePoint> getKnowledgePoints() { return knowledgePoints; }
    public void setKnowledgePoints(List<KnowledgePoint> knowledgePoints) { this.knowledgePoints = knowledgePoints; }

    public List<Submission> getSubmissions() { return submissions; }
    public void setSubmissions(List<Submission> submissions) { this.submissions = submissions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
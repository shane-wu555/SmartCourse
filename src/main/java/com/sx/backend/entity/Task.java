package com.sx.backend.entity;

import java.time.LocalDateTime;
import java.util.List;

public class Task {
    private String taskId;
    private Course course;
    private String title;
    private TaskType type;
    private LocalDateTime deadline;
    private Float maxScore;
    private List<Resource> resources;
    private List<Submission> submissions; // 学生提交
    //private List<knowledgePoint> knowledgePoints;


    public Task(Course course, String taskId, String title, TaskType type, LocalDateTime deadline, Float maxScore, List<Resource> resources) {
        this.course = course;
        this.taskId = taskId;
        this.title = title;
        this.type = type;
        this.deadline = deadline;
        this.maxScore = maxScore;
        this.resources = resources;
    }

    public Task() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public Float getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Float maxScore) {
        this.maxScore = maxScore;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }
}

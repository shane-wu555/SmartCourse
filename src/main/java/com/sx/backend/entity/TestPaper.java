package com.sx.backend.entity;

import java.util.List;

public class TestPaper {
    private String paperId;
    private String taskId;
    private String title;
    private List<String> questions;
    private Float totalScore;
    private Integer timeLimit; // 以分钟为单位
    private PaperGenerationMethod generationMethod;

    public TestPaper(String paperId, String taskId, String title, List<String> questions, Float totalScore, Integer timeLimit, PaperGenerationMethod generationMethod) {
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

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
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

    // 设置组卷方式
    public void setGenerationMethod(PaperGenerationMethod generationMethod) {
        this.generationMethod = generationMethod;
    }

    // 设置总分（自动计算）
    public void calculateTotalScore() {
        if (questions != null && !questions.isEmpty()) {
            float sum = 0f;
            for (Question q : questions) {
                if (q.getScore() != null) sum += q.getScore();
            }
            this.totalScore = sum;
        } else {
            this.totalScore = 0f;
        }
    }

    // 设置课程ID（通过Task）
    public void setCourseId(String courseId) {
        if (this.task == null) this.task = new Task();
        if (this.task.getCourse() == null) this.task.setCourse(new Course());
        this.task.getCourse().setCourseId(courseId);
    }
}

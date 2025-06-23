package com.sx.backend.entity;

import java.util.List;

public class TestPaper {
    private String paperId;
    private Task task;
    private String title;
    private List<Question> questions;
    private Float totalScore;
    private Integer timeLimit; // 以分钟为单位
    private PaperGenerationMethod generationMethod;

    public TestPaper(String paperId, Task task, String title, List<Question> questions, Float totalScore, Integer timeLimit, PaperGenerationMethod generationMethod) {
        this.paperId = paperId;
        this.task = task;
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

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
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

    public void setGenerationMethod(PaperGenerationMethod generationMethod) {
        this.generationMethod = generationMethod;
    }
}

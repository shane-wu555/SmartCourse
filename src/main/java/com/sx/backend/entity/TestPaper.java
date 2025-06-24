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

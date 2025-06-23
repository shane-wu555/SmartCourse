package com.sx.backend.entity;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class Grade {
    private String gradeId;
    private Student student;
    private Course course;
    private List<TaskGrade> taskGrades; // 各任务成绩
    private Float finalGrade;           // 总成绩
    private String feedback;
    private JsonNode gradeTrend;        // 成绩趋势数据
    private Integer rankInClass;        // 班级排名

    public Grade(String gradeId, Student student, Course course, List<TaskGrade> taskGrades, Float finalGrade, String feedback, JsonNode gradeTrend, Integer rankInClass) {
        this.gradeId = gradeId;
        this.student = student;
        this.course = course;
        this.taskGrades = taskGrades;
        this.finalGrade = finalGrade;
        this.feedback = feedback;
        this.gradeTrend = gradeTrend;
        this.rankInClass = rankInClass;
    }

    public Grade() {
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<TaskGrade> getTaskGrades() {
        return taskGrades;
    }

    public void setTaskGrades(List<TaskGrade> taskGrades) {
        this.taskGrades = taskGrades;
    }

    public Float getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(Float finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public JsonNode getGradeTrend() {
        return gradeTrend;
    }

    public void setGradeTrend(JsonNode gradeTrend) {
        this.gradeTrend = gradeTrend;
    }

    public Integer getRankInClass() {
        return rankInClass;
    }

    public void setRankInClass(Integer rankInClass) {
        this.rankInClass = rankInClass;
    }
}

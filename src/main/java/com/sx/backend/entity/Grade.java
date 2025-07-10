package com.sx.backend.entity;

import java.util.List;

public class Grade {
    private String gradeId;
    private String studentId;
    private String courseId;
    private List<TaskGrade> taskGrades; // 各任务成绩
    private Float finalGrade;           // 总成绩
    private String feedback;
    private String gradeTrend;        // 成绩趋势数据
    private Integer rankInClass;        // 班级排名

    public Grade(String gradeId, String student, String course, List<TaskGrade> taskGrades, Float finalGrade, String feedback, String gradeTrend, Integer rankInClass) {
        this.gradeId = gradeId;
        this.studentId = student;
        this.courseId = course;
        this.taskGrades = taskGrades;
        this.finalGrade = finalGrade;
        this.feedback = feedback;
        this.gradeTrend = gradeTrend;
        this.rankInClass = rankInClass;
    }

    public Grade() {
    }

    public Grade(String id, String studentId, String courseId, float finalGrade, int rankInClass) {
        this.gradeId = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.finalGrade = finalGrade;
        this.rankInClass = rankInClass;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public String getGradeTrend() {
        return gradeTrend;
    }

    public void setGradeTrend(String gradeTrend) {
        this.gradeTrend = gradeTrend;
    }

    public Integer getRankInClass() {
        return rankInClass;
    }

    public void setRankInClass(Integer rankInClass) {
        this.rankInClass = rankInClass;
    }
}

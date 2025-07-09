package com.sx.backend.dto;

import java.util.List;
import java.util.Map;

public class GradeTrendDTO {
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseName;
    private List<String> dates;
    private List<Float> scores;
    private List<Float> totalScores; // 新增：每次任务的总分
    private List<String> taskNames; // 新增：任务名称数组
    private String chartImage;
    private Map<String, Double> taskCompletion;

    public GradeTrendDTO(String studentId, String studentName, String courseId, String courseName, List<String> dates, List<Float> scores, List<Float> totalScores, List<String> taskNames, String chartImage, Map<String, Double> taskCompletion) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseId = courseId;
        this.courseName = courseName;
        this.dates = dates;
        this.scores = scores;
        this.totalScores = totalScores;
        this.taskNames = taskNames;
        this.chartImage = chartImage;
        this.taskCompletion = taskCompletion;
    }

    public GradeTrendDTO() {
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Float> getScores() {
        return scores;
    }

    public void setScores(List<Float> scores) {
        this.scores = scores;
    }

    public List<Float> getTotalScores() {
        return totalScores;
    }

    public void setTotalScores(List<Float> totalScores) {
        this.totalScores = totalScores;
    }

    public String getChartImage() {
        return chartImage;
    }

    public void setChartImage(String chartImage) {
        this.chartImage = chartImage;
    }

    public Map<String, Double> getTaskCompletion() {
        return taskCompletion;
    }

    public void setTaskCompletion(Map<String, Double> taskCompletion) {
        this.taskCompletion = taskCompletion;
    }

    public List<String> getTaskNames() {
        return taskNames;
    }

    public void setTaskNames(List<String> taskNames) {
        this.taskNames = taskNames;
    }
}

package com.sx.backend.dto;

public class FeedbackDTO {
    private String studentId;
    private String courseId;
    private float finalGrade;
    private int rankInClass;
    private String message;

    public FeedbackDTO(String studentId, String courseId, float finalGrade, int rankInClass, String message) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.finalGrade = finalGrade;
        this.rankInClass = rankInClass;
        this.message = message;
    }

    public FeedbackDTO() {
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

    public float getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(float finalGrade) {
        this.finalGrade = finalGrade;
    }

    public int getRankInClass() {
        return rankInClass;
    }

    public void setRankInClass(int rankInClass) {
        this.rankInClass = rankInClass;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

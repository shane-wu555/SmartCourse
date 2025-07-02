package com.sx.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Submission {
    private String submissionId;
    private String taskId;
    private String studentId;
    private LocalDateTime submitTime;
    private SubmissionStatus status = SubmissionStatus.SUBMITTED; // 默认状态
    private List<String> files; // 上传的文件url
    private List<String> answerRecords = new ArrayList<>(); // 学生答案id记录
    private boolean completed; // 是否完成
    private Float finalGrade; // 总成绩
    private Float autoGrade; // 自动批改成绩
    private String feedback; // 教师反馈
    private LocalDateTime gradeTime; // 批改时间

    public Submission(String submissionId, String taskId, String studentId, LocalDateTime submitTime,
                      SubmissionStatus status, List<String> files, Float finalGrade,
                      String feedback, LocalDateTime gradeTime) {
        this.submissionId = submissionId;
        this.taskId = taskId;
        this.studentId = studentId;
        this.submitTime = submitTime;
        this.status = status;
        this.files = files;
        this.finalGrade = finalGrade;
        this.feedback = feedback;
        this.gradeTime = gradeTime;
    }

    public Submission() {
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public Float getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(Float finalGrade) {
        this.finalGrade = finalGrade;
    }

    public List<String> getAnswerRecords() {
        return answerRecords;
    }

    public void setAnswerRecords(List<String> answerRecords) {
        this.answerRecords = answerRecords;
    }

    public Float getAutoGrade() {
        return autoGrade;
    }

    public void setAutoGrade(Float autoGrade) {
        this.autoGrade = autoGrade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getGradeTime() {
        return gradeTime;
    }

    public void setGradeTime(LocalDateTime gradeTime) {
        this.gradeTime = gradeTime;
    }
}


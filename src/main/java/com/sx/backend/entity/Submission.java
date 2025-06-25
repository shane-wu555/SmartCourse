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
    private String content; // 文本内容
    private List<FileMeta> files; // 上传的文件
    private List<String> answerRecords = new ArrayList<>(); // 学生答案记录
    private Float finalGrade; // 总成绩
    private Float autoGrade; // 自动批改成绩
    private String feedback; // 教师反馈
    private LocalDateTime gradeTime; // 批改时间

    public Submission(String submissionId, String taskId, String studentId, LocalDateTime submitTime,
                      SubmissionStatus status, String content, List<FileMeta> files, Float finalGrade,
                      String feedback, LocalDateTime gradeTime) {
        this.submissionId = submissionId;
        this.taskId = taskId;
        this.studentId = studentId;
        this.submitTime = submitTime;
        this.status = status;
        this.content = content;
        this.files = files;
        this.finalGrade = finalGrade;
        this.feedback = feedback;
        this.gradeTime = gradeTime;
    }

    public Submission() {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FileMeta> getFiles() {
        return files;
    }

    public void setFiles(List<FileMeta> files) {
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


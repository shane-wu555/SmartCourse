package com.sx.backend.entity;

import ch.qos.logback.core.joran.sanity.Pair;
import com.sx.backend.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Submission {
    private String submissionId;
    private String taskId;
    private String studentId;
    private LocalDateTime submitTime;
    private SubmissionStatus status = SubmissionStatus.SUBMITTED; // 默认状态
    private String content; // 文本内容
    private List<FileMeta> files; // 上传的文件
    private List<AnswerRecord> answerRecords = new ArrayList<>(); // 学生答案记录
    private Float grade; // 总成绩
    private Float autoGrade; // 自动批改成绩
    private String feedback; // 教师反馈
    private LocalDateTime gradeTime; // 批改时间

    public Submission(String submissionId, String taskId, String studentId, LocalDateTime submitTime,
                      SubmissionStatus status, String content, List<FileMeta> files, Float grade,
                      String feedback, LocalDateTime gradeTime) {
        this.submissionId = submissionId;
        this.taskId = taskId;
        this.studentId = studentId;
        this.submitTime = submitTime;
        this.status = status;
        this.content = content;
        this.files = files;
        this.grade = grade;
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

    public Float getGrade() {
        return grade;
    }

    public void setGrade(Float grade) {
        this.grade = grade;
    }

    public List<AnswerRecord> getAnswerRecords() {
        return answerRecords;
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

    // 自动批改
    public Float autoGradePaper(List<AnswerRecord> answerRecords) {
        Float autoScore = 0.0f;
        for (AnswerRecord record : answerRecords) {
            if (record.getQuestion().isAutoGradable()) {
                record.autoGrade();
                autoScore += record.getObtainedScore();
            }
        }

        return autoScore;
    }

    // 获取需要手动批改的题目
    public List<AnswerRecord> getQuestionForManualGrading(List<AnswerRecord> answerRecords) {
        return answerRecords.stream()
                .filter(record -> !record.isAutoGraded())
                .collect(Collectors.toList());
    }

    // 处理学生提交的试卷
    public void submitTestPaper(List<AnswerRecord> answerRecords) {
        this.answerRecords = answerRecords;
        this.submitTime = LocalDateTime.now();

        // 自动批改
        this.autoGrade = autoGradePaper(answerRecords);
        this.grade = autoGrade;

        // 更新提交状态
        boolean hasManualGrading = !getQuestionForManualGrading(answerRecords).isEmpty();
        this.status = hasManualGrading ? SubmissionStatus.AUTO_GRADED : SubmissionStatus.GRADED;
    }

    // 老师批改
    public void gradeManualQuestions(Map<String, Pair<Float, String>> questionGrades, String feedback) {
        List<AnswerRecord> manualRecords = getQuestionForManualGrading(answerRecords);

        for (AnswerRecord record : manualRecords) {
            Float score = questionGrades.get(record.getRecordId()).first;
            String questionFeedback = questionGrades.get(record.getRecordId()).second;
            if (score != null) {
                record.manualGrade(score, questionFeedback);
                this.grade += score;
            }
        }

        this.status = SubmissionStatus.GRADED;
        this.feedback = feedback;
    }
}


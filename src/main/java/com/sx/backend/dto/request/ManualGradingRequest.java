package com.sx.backend.dto.request;

import ch.qos.logback.core.joran.sanity.Pair;

import java.util.Map;

public class ManualGradingRequest {
    private Map<String, Pair<Float, String>> questionGrades; // 每个问题的分数和教师反馈
    private String feedback; // 总的教师反馈

    public ManualGradingRequest(Map<String, Pair<Float, String>> questionGrades, String feedback) {
        this.questionGrades = questionGrades;
        this.feedback = feedback;
    }

    public ManualGradingRequest() {
    }

    public Map<String, Pair<Float, String>> getQuestionGrades() {
        return questionGrades;
    }

    public void setQuestionGrades(Map<String, Pair<Float, String>> questionGrades) {
        this.questionGrades = questionGrades;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}

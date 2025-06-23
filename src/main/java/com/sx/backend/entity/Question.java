package com.sx.backend.entity;

import java.util.List;
import java.util.Set;

public class Question {
    private String questionId;
    private String content;
    private QuestionType type;
    private Set<String> options; // 仅选择题和判断题有选项
    private Set<String> answer; // 选择题答案或判断题答案
    private Float score;
    private DifficultyLevel difficulty;
    private List<KnowledgePoint> knowledgePoints;

    public Question(String questionId, String content, QuestionType type, Set<String> options, Set<String> answer, Float score, DifficultyLevel difficulty, List<KnowledgePoint> knowledgePoints) {
        this.questionId = questionId;
        this.content = content;
        this.type = type;
        this.options = options;
        this.answer = answer;
        this.score = score;
        this.difficulty = difficulty;
        this.knowledgePoints = knowledgePoints;
    }

    public Question() {
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public Set<String> getOptions() {
        return options;
    }

    public void setOptions(Set<String> options) {
        this.options = options;
    }

    public Set<String> getAnswer() {
        return answer;
    }

    public void setAnswer(Set<String> answer) {
        this.answer = answer;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public List<KnowledgePoint> getKnowledgePoints() {
        return knowledgePoints;
    }

    public void setKnowledgePoints(List<KnowledgePoint> knowledgePoints) {
        this.knowledgePoints = knowledgePoints;
    }

    // 判断是否可以自动评分
    public boolean isAutoGradable() {
        return type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.SINGLE_CHOICE || type == QuestionType.JUDGE || type == QuestionType.FILL_BLANK;
    }

    // 自动评分逻辑
    public Float autoGrade(Set<String> studentAnswer) {
        if (!isAutoGradable()) {
            throw new UnsupportedOperationException("此题型不支持自动评分");
        }

        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.JUDGE) {
            return answer.equals(studentAnswer) ? score : 0f;
        } else if (type == QuestionType.FILL_BLANK) {
            // 填空题可以有多个正确答案
            for (String answer : studentAnswer) {
                if (this.answer.contains(answer)) {
                    return score; // 只要有一个答案正确就给分
                }
            }

            return 0f;
        }

        return 0f; // 默认返回0分
    }
}

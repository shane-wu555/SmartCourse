package com.sx.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class Question {
    private String questionId; // 主键
    private String bankId; // 课程ID
    private String content; // 题干内容
    private QuestionType type; // 题型
    private List<String> options; // 仅选择题和判断题有选项
    private String answer; // 选择题答案或判断题答案
    private Float score;
    private DifficultyLevel difficultylevel;
    private List<KnowledgePoint> knowledgePoints;

    public Question() {
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

        if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.JUDGE) {
            return studentAnswer.size() == 1 && answer.equals(studentAnswer.iterator().next()) ? score : 0f;
        } else if (type == QuestionType.MULTIPLE_CHOICE) {
            // 多选题答案通常是无序集合
            return new HashSet<>(Arrays.asList(answer.split(";"))).equals(studentAnswer) ? score : 0f;
        }

        return 0f; // 默认返回0分
    }
}

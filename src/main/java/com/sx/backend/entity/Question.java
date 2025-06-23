package com.sx.backend.entity;

import lombok.Data;

import java.util.List;

@Data
public class Question {
    private String questionId; // 主键
    private String bankId; // 课程ID
    private String content; // 题干内容
    private QuestionType type; // 题型
    private List<String> options; // 选择题选项
    private String answer; // 标准答案
    private Float score; // 分值
    private DifficultyLevel difficultylevel; // 难度
    private List<KnowledgePoint> knowledgePoints;

}

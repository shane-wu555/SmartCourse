package com.sx.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private List<String> knowledgePoints; // 知识点ID列表

    public Question() {
    }

    // 判断是否可以自动评分 - 添加@JsonIgnore避免序列化
    @JsonIgnore
    public boolean isAutoGradable() {
        return type == QuestionType.MULTIPLE_CHOICE || type == QuestionType.SINGLE_CHOICE || type == QuestionType.JUDGE;
    }

    // 自动评分逻辑
    public Float autoGrade(List<String> studentAnswer) {
        if (!isAutoGradable()) {
            throw new UnsupportedOperationException("此题型不支持自动评分");
        }

        System.out.println("DEBUG: 开始自动评分 - 题目类型: " + type + ", 学生答案: " + studentAnswer + ", 正确答案: " + answer);

        if (type == QuestionType.SINGLE_CHOICE) {
            // 单选题：将学生的选项标识符（A、B、C等）转换为对应的选项内容
            if (studentAnswer.size() != 1) {
                System.out.println("DEBUG: 单选题答案数量不正确: " + studentAnswer.size());
                return 0f;
            }
            String studentChoice = studentAnswer.get(0);
            String actualAnswer = getOptionContent(studentChoice);
            System.out.println("DEBUG: 单选题 - 学生选择: " + studentChoice + ", 转换后: " + actualAnswer + ", 正确答案: " + answer);
            boolean correct = answer.equals(actualAnswer);
            System.out.println("DEBUG: 单选题评分结果: " + (correct ? score : 0f));
            return correct ? score : 0f;
        } else if (type == QuestionType.JUDGE) {
            // 判断题：处理A/B或者是/否的映射
            if (studentAnswer.size() != 1) {
                System.out.println("DEBUG: 判断题答案数量不正确: " + studentAnswer.size());
                return 0f;
            }
            String studentChoice = studentAnswer.get(0);
            String actualAnswer = getJudgeAnswer(studentChoice);
            System.out.println("DEBUG: 判断题 - 学生选择: " + studentChoice + ", 转换后: " + actualAnswer + ", 正确答案: " + answer);
            boolean correct = answer.equals(actualAnswer);
            System.out.println("DEBUG: 判断题评分结果: " + (correct ? score : 0f));
            return correct ? score : 0f;
        } else if (type == QuestionType.MULTIPLE_CHOICE) {
            // 多选题：将学生的选项标识符转换为对应的选项内容
            Set<String> studentChoices = new HashSet<>();
            for (String choice : studentAnswer) {
                String actualChoice = getOptionContent(choice);
                if (actualChoice != null) {
                    studentChoices.add(actualChoice);
                }
                System.out.println("DEBUG: 多选题选项转换 - " + choice + " -> " + actualChoice);
            }
            
            // 将标准答案按分号分割并转换为Set
            Set<String> correctAnswers = new HashSet<>(Arrays.asList(answer.split(";")));
            System.out.println("DEBUG: 多选题 - 学生答案集合: " + studentChoices + ", 正确答案集合: " + correctAnswers);
            boolean correct = correctAnswers.equals(studentChoices);
            System.out.println("DEBUG: 多选题评分结果: " + (correct ? score : 0f));
            return correct ? score : 0f;
        }

        return 0f; // 默认返回0分
    }
    
    // 根据选项标识符获取选项内容
    private String getOptionContent(String optionLabel) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        
        // 处理A、B、C、D等选项标识符
        int index = -1;
        if (optionLabel.length() == 1) {
            char label = optionLabel.charAt(0);
            if (label >= 'A' && label <= 'Z') {
                index = label - 'A';
            } else if (label >= 'a' && label <= 'z') {
                index = label - 'a';
            }
        }
        
        if (index >= 0 && index < options.size()) {
            return options.get(index);
        }
        
        return null;
    }
    
    // 处理判断题答案映射
    private String getJudgeAnswer(String studentChoice) {
        if ("A".equals(studentChoice) || "a".equals(studentChoice)) {
            // 通常A代表"正确"或"是"
            return "正确";
        } else if ("B".equals(studentChoice) || "b".equals(studentChoice)) {
            // 通常B代表"错误"或"否"
            return "错误";
        }
        
        // 如果选项中有具体的判断选项，也尝试匹配
        String optionContent = getOptionContent(studentChoice);
        if (optionContent != null) {
            return optionContent;
        }
        
        return studentChoice; // 如果都不匹配，返回原始选择
    }

    // 检查是否包含指定的知识点ID - 静态方法，不会被序列化
    public static boolean containsKnowledgePoint(List<String> knowledgePoints, List<String> targetIds) {
        if (knowledgePoints == null || targetIds == null) {
            return false;
        }
        for (String kpId : knowledgePoints) {
            if (targetIds.contains(kpId)) {
                return true;
            }
        }
        return false;
    }

    // 获取题目主知识点ID（第一个知识点ID，若有）- 添加@JsonIgnore避免序列化
    @JsonIgnore
    public String getMainKnowledgePointId() {
        if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
            return knowledgePoints.get(0);
        }
        return null;
    }

    // 获取题目难度等级（字符串）- 添加@JsonIgnore避免序列化
    @JsonIgnore
    public String getDifficultyLevel() {
        return difficultylevel != null ? difficultylevel.name().toLowerCase() : null;
    }

    // 获取题目类型（字符串）- 添加@JsonIgnore避免序列化
    @JsonIgnore
    public String getTypeString() {
        return type != null ? type.name() : null;
    }
}

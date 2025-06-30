package com.sx.backend.dto;

import java.util.List;

/**
 * 智能组卷请求DTO
 */
public class GeneratePaperRequestDTO {
    /** 课程ID */
    private String courseId;
    /** 题库ID */
    private String bankId;
    /** 组卷方式：random/knowledge/difficulty */
    private String mode;
    /** 题目总数 */
    private Integer totalCount;
    /** 知识点ID列表（按知识点组卷时用） */
    private List<String> knowledgePointIds;
    /** 题型列表（按题型组卷时用） */
    private List<String> questionTypes;
    /** 难度分布（如：{"easy":5,"medium":3,"hard":2}） */
    private DifficultyDistribution difficultyDistribution;

    public static class DifficultyDistribution {
        private Integer easy;
        private Integer medium;
        private Integer hard;

        public Integer getEasy() { return easy; }
        public void setEasy(Integer easy) { this.easy = easy; }
        public Integer getMedium() { return medium; }
        public void setMedium(Integer medium) { this.medium = medium; }
        public Integer getHard() { return hard; }
        public void setHard(Integer hard) { this.hard = hard; }
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<String> getKnowledgePointIds() {
        return knowledgePointIds;
    }

    public void setKnowledgePointIds(List<String> knowledgePointIds) {
        this.knowledgePointIds = knowledgePointIds;
    }

    public List<String> getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(List<String> questionTypes) {
        this.questionTypes = questionTypes;
    }

    public DifficultyDistribution getDifficultyDistribution() {
        return difficultyDistribution;
    }

    public void setDifficultyDistribution(DifficultyDistribution difficultyDistribution) {
        this.difficultyDistribution = difficultyDistribution;
    }
}

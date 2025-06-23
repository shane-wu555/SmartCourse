package com.sx.backend.entity;

public enum QuestionType {
    SINGLE_CHOICE("单选题"),
    MULTIPLE_CHOICE("多选题"),
    JUDGE("判断题"),
    FILL_BLANK("填空题"),
    SHORT_ANSWER("简答题"),
    PROGRAMMING("编程题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

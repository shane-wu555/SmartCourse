package com.sx.backend.entity;

public enum PaperGenerationMethod {
    RANDOM("随机组卷"),
    BY_KNOWLEDGE("按知识点组卷"),
    DIFFICULTY_BALANCE("按难度平衡");

    private final String description;

    PaperGenerationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package com.sx.backend.entity;

// 提交状态枚举
public enum SubmissionStatus {
    SUBMITTED("已提交"),
    GRADED("已批改"),
    RETURNED("已退回");

    private final String displayName;

    SubmissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


}

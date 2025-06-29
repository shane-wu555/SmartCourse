package com.sx.backend.entity;

public enum TaskType {
    CHAPTER_HOMEWORK("章节作业", "文件上传"),
    EXAM_QUIZ("试卷答题", "在线测试自动批改"),
    VIDEO_WATCHING("视频观看", "自动记录学习进度"),
    MATERIAL_READING("阅读材料", "PDF/文档阅读"),
    PPT_VIEW("PPT浏览", "幻灯片查看"),
    REPORT_SUBMISSION("实践项目", "文件上传(.doc/.docx/.pdf)");

    private final String displayName;
    private final String description;

    TaskType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

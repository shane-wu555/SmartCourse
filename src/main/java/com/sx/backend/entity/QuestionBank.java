package com.sx.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuestionBank {
    private String bankId;           // 主键
    private String name;             // 题库名称
    private String description;      // 题库描述
    private List<Question> questions; // 题库下所有题目
    private String creatorId;        // 创建者（教师）ID
    private String courseId;      // 创建者（教师）姓名
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

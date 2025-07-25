package com.sx.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePoint {
    private String pointId;
    private String courseId;
    private String name;
    private String description;
    private DifficultyLevel difficultylevel; // 使用枚举类型
    private Date createdAt;
    private Date updatedAt;
    private Integer resourceCount;
}
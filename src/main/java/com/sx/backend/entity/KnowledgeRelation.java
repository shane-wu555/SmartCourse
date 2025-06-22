package com.sx.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelation {
    private String relationId;
    private String sourcePointId;
    private String targetPointId;
    private RelationType relationType; // 使用枚举类型
    private Date createdAt;
}


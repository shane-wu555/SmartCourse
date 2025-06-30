package com.sx.backend.entity;

/**
 * 知识点关系类型枚举
 * 对应数据库中的 relation_type 字段
 */
public enum RelationType {
    PREREQUISITE,  // 先修关系
    RELATED,       // 相关关系
    PART_OF        // 包含关系
}

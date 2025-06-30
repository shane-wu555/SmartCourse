package com.sx.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeGraphDTO {
    private List<Node> nodes;
    private List<Edge> edges;

    @Data
    public static class Node {
        private String id;
        private String name;
        private String description;
        private String difficultylevel;
        private String courseId;      // 新增：所属课程ID
        // 可扩展更多字段
    }

    @Data
    public static class Edge {
        private String source;
        private String target;
        private String type; // 中文关系
        private String relationType;  // 新增：关系类型（先修，包含，相关）
        // 可扩展更多字段
    }
}
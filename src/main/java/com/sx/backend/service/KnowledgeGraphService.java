package com.sx.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.KnowledgeRelation;
import com.sx.backend.entity.RelationType;
import com.sx.backend.mapper.KnowledgePointMapper;
import com.sx.backend.mapper.KnowledgeRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@Service
public class KnowledgeGraphService {
    
    @Autowired
    private KnowledgePointMapper knowledgePointMapper;
    
    @Autowired
    private KnowledgeRelationMapper knowledgeRelationMapper;
    
    private final OllamaService ollamaService = new OllamaService();

    /**
     * 基于已有知识点生成知识图谱关系
     * @param courseId 课程ID
     * @return 生成的知识图谱
     */
    @Transactional
    public KnowledgeGraphDTO generateGraphForCourse(String courseId) throws Exception {
        // 1. 从数据库获取该课程的所有知识点
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            throw new RuntimeException("该课程没有知识点，无法生成知识图谱");
        }
        
        // 2. 调用大模型生成知识点关系
        String response = ollamaService.generateKnowledgeRelations(knowledgePoints);
        String json = extractJson(response);
        
        // 3. 解析大模型返回的JSON
        ObjectMapper mapper = new ObjectMapper();
        KnowledgeGraphDTO graphDto = mapper.readValue(json, KnowledgeGraphDTO.class);
        
        // 4. 保存生成的关系到数据库
        saveRelationsToDatabase(graphDto.getEdges(), courseId);
        
        // 5. 返回完整的知识图谱数据
        return getKnowledgeGraphFromDatabase(courseId);
    }

    /**
     * 原有方法：基于内容生成知识图谱（保持兼容性）
     */
    public KnowledgeGraphDTO generateGraph(String content) throws Exception {
        String response = ollamaService.extractKnowledgePoints(content);
        String json = extractJson(response);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, KnowledgeGraphDTO.class);
    }
    
    /**
     * 从数据库获取完整的知识图谱数据
     */
    public KnowledgeGraphDTO getKnowledgeGraphFromDatabase(String courseId) {
        // 获取知识点
        List<KnowledgePoint> points = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        List<KnowledgeGraphDTO.Node> nodes = points.stream().map(point -> {
            KnowledgeGraphDTO.Node node = new KnowledgeGraphDTO.Node();
            node.setId(point.getPointId());
            node.setName(point.getName());
            node.setDescription(point.getDescription());
            node.setDifficultyLevel(point.getDifficultylevel() != null ? point.getDifficultylevel().toString() : "");
            node.setCourseId(point.getCourseId());
            return node;
        }).toList();
        
        // 获取关系
        List<KnowledgeRelation> relations = knowledgeRelationMapper.selectRelationsByCourseId(courseId);
        List<KnowledgeGraphDTO.Edge> edges = relations.stream().map(rel -> {
            KnowledgeGraphDTO.Edge edge = new KnowledgeGraphDTO.Edge();
            edge.setSource(rel.getSourcePointId());
            edge.setTarget(rel.getTargetPointId());
            edge.setRelationType(rel.getRelationType().toString().toLowerCase());
            edge.setType(convertRelationTypeToChines(rel.getRelationType()));
            return edge;
        }).toList();
        
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        dto.setNodes(nodes);
        dto.setEdges(edges);
        return dto;
    }
    
    /**
     * 保存关系到数据库
     */
    private void saveRelationsToDatabase(List<KnowledgeGraphDTO.Edge> edges, String courseId) {
        // 创建知识点ID映射，验证ID是否存在
        List<KnowledgePoint> points = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        Map<String, KnowledgePoint> pointMap = new HashMap<>();
        for (KnowledgePoint point : points) {
            pointMap.put(point.getPointId(), point);
        }
        
        // 保存新的关系（避免重复关系）
        for (KnowledgeGraphDTO.Edge edge : edges) {
            // 验证source和target知识点是否存在
            if (!pointMap.containsKey(edge.getSource()) || !pointMap.containsKey(edge.getTarget())) {
                continue; // 跳过无效的关系
            }
            
            // 检查关系是否已存在
            if (knowledgeRelationMapper.checkRelationExists(edge.getSource(), edge.getTarget()) > 0) {
                continue; // 跳过已存在的关系
            }
            
            KnowledgeRelation relation = new KnowledgeRelation();
            relation.setRelationId(UUID.randomUUID().toString());
            relation.setSourcePointId(edge.getSource());
            relation.setTargetPointId(edge.getTarget());
            relation.setRelationType(convertStringToRelationType(edge.getRelationType()));
            relation.setCreatedAt(new Date());
            
            knowledgeRelationMapper.insertKnowledgeRelation(relation);
        }
    }
    
    /**
     * 将字符串转换为RelationType枚举
     */
    private RelationType convertStringToRelationType(String relationType) {
        switch (relationType.toLowerCase()) {
            case "prerequisite":
                return RelationType.PREREQUISITE;
            case "related":
                return RelationType.RELATED;
            case "part-of":
                return RelationType.DEPENDENCY; // 使用DEPENDENCY表示包含关系
            default:
                return RelationType.RELATED;
        }
    }
    
    /**
     * 将RelationType转换为中文
     */
    private String convertRelationTypeToChines(RelationType relationType) {
        switch (relationType) {
            case PREREQUISITE:
                return "先修";
            case RELATED:
                return "相关";
            case DEPENDENCY:
                return "包含";
            default:
                return "相关";
        }
    }
    
    private String extractJson(String response) {
        // 优先提取 ```json ... ``` 代码块中的内容
        int codeStart = response.indexOf("```json");
        if (codeStart != -1) {
            int jsonStart = response.indexOf("{", codeStart);
            int jsonEnd = response.lastIndexOf("}");
            if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                return response.substring(jsonStart, jsonEnd + 1);
            }
        }
        
        // 否则，提取第一个 { 到最后一个 } 之间的内容
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        if (start != -1 && end > start) {
            return response.substring(start, end);
        }
        
        return response;
    }
}

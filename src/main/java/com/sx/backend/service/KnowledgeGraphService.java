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
        System.out.println("开始为课程生成知识图谱关系: " + courseId);

        // 1. 从数据库获取该课程的所有知识点
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);

        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            throw new RuntimeException("该课程没有知识点，无法生成知识图谱");
        }

        System.out.println("找到知识点数量: " + knowledgePoints.size());
        for (KnowledgePoint point : knowledgePoints) {
            System.out.println("知识点: " + point.getPointId() + " - " + point.getName());
        }

        // 2. 调用大模型生成知识点关系
        System.out.println("开始调用AI生成关系...");
        String response = ollamaService.generateKnowledgeRelations(knowledgePoints);
        System.out.println("AI原始响应: " + response);

        String json = extractJson(response);
        System.out.println("提取的JSON: " + json);

        // 3. 解析大模型返回的JSON
        ObjectMapper mapper = new ObjectMapper();
        KnowledgeGraphDTO graphDto;
        try {
            graphDto = mapper.readValue(json, KnowledgeGraphDTO.class);
            System.out.println("JSON解析成功");
            System.out.println("节点数量: " + (graphDto.getNodes() != null ? graphDto.getNodes().size() : 0));
            System.out.println("边数量: " + (graphDto.getEdges() != null ? graphDto.getEdges().size() : 0));

            if (graphDto.getEdges() != null) {
                for (KnowledgeGraphDTO.Edge edge : graphDto.getEdges()) {
                    System.out.println("Edge: " + edge.getSource() + " -> " + edge.getTarget() + " (" + edge.getRelationType() + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("解析AI返回的JSON失败: " + e.getMessage());
        }

        // 4. 保存生成的关系到数据库
        if (graphDto.getEdges() != null && !graphDto.getEdges().isEmpty()) {
            System.out.println("开始保存关系到数据库...");
            saveRelationsToDatabase(graphDto.getEdges(), courseId);
            System.out.println("关系保存完成");
        } else {
            System.out.println("没有找到边关系数据，跳过保存");
        }

        // 5. 返回完整的知识图谱数据
        System.out.println("从数据库获取最终的知识图谱数据...");
        KnowledgeGraphDTO result = getKnowledgeGraphFromDatabase(courseId);
        System.out.println("最终结果 - 节点: " + result.getNodes().size() + ", 边: " + result.getEdges().size());

        return result;
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
            node.setDifficultylevel(point.getDifficultylevel() != null ? point.getDifficultylevel().toString() : "");
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
            System.out.println("处理边: " + edge.getSource() + " -> " + edge.getTarget() + " (关系类型: " + edge.getRelationType() + ")");
            
            // 验证source和target知识点是否存在
            if (!pointMap.containsKey(edge.getSource()) || !pointMap.containsKey(edge.getTarget())) {
                System.err.println("跳过无效关系 - 知识点不存在: " + edge.getSource() + " -> " + edge.getTarget());
                continue; // 跳过无效的关系
            }
            
            // 检查关系是否已存在
            if (knowledgeRelationMapper.checkRelationExists(edge.getSource(), edge.getTarget()) > 0) {
                System.out.println("跳过已存在的关系: " + edge.getSource() + " -> " + edge.getTarget());
                continue; // 跳过已存在的关系
            }
            
            KnowledgeRelation relation = new KnowledgeRelation();
            relation.setRelationId(UUID.randomUUID().toString());
            relation.setSourcePointId(edge.getSource());
            relation.setTargetPointId(edge.getTarget());
            RelationType relationType = convertStringToRelationType(edge.getRelationType());
            relation.setRelationType(relationType);
            relation.setCreatedAt(new Date());
            
            System.out.println("保存关系: " + edge.getSource() + " -> " + edge.getTarget() + " (" + relationType + ")");
            knowledgeRelationMapper.insertKnowledgeRelation(relation);
        }
    }
    
    /**
     * 将字符串转换为RelationType枚举
     */
    private RelationType convertStringToRelationType(String relationType) {
        // 先转为大写再匹配，兼容大小写
        switch (relationType.toUpperCase()) {
            case "PREREQUISITE":
                return RelationType.PREREQUISITE;
            case "RELATED":
                return RelationType.RELATED;
            case "PART_OF":
            case "PART-OF":  // 兼容带连字符的形式
                return RelationType.PART_OF;
            default:
                System.err.println("未知的关系类型: " + relationType + "，使用默认值 RELATED");
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
            case PART_OF:
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
    
    /**
     * 测试方法：验证AI生成的关系
     */
    public void testAIGeneration(String courseId) throws Exception {
        List<KnowledgePoint> knowledgePoints = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);
        System.out.println("测试AI生成 - 知识点数量: " + knowledgePoints.size());
        
        String response = ollamaService.generateKnowledgeRelations(knowledgePoints);
        System.out.println("=== AI完整响应 ===");
        System.out.println(response);
        System.out.println("=== 响应结束 ===");
        
        String json = extractJson(response);
        System.out.println("=== 提取的JSON ===");
        System.out.println(json);
        System.out.println("=== JSON结束 ===");
        
        // 尝试解析JSON
        try {
            ObjectMapper mapper = new ObjectMapper();
            KnowledgeGraphDTO graphDto = mapper.readValue(json, KnowledgeGraphDTO.class);
            System.out.println("JSON解析成功！");
            System.out.println("节点数: " + (graphDto.getNodes() != null ? graphDto.getNodes().size() : 0));
            System.out.println("边数: " + (graphDto.getEdges() != null ? graphDto.getEdges().size() : 0));
        } catch (Exception e) {
            System.err.println("JSON解析失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

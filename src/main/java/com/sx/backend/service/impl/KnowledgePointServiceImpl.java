package com.sx.backend.service.impl;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.KnowledgeRelation;
import com.sx.backend.entity.RelationType;
import com.sx.backend.entity.Resource;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.CourseMapper;
import com.sx.backend.mapper.KnowledgePointMapper;
import com.sx.backend.mapper.KnowledgeRelationMapper;
import com.sx.backend.mapper.ResourceMapper;
import com.sx.backend.service.KnowledgePointService;
import com.sx.backend.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.sx.backend.entity.DifficultyLevel.MEDIUM;
import com.sx.backend.dto.KnowledgeGraphDTO;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import java.util.concurrent.CompletableFuture;

@Service
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgePointServiceImpl.class);

    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgeRelationMapper knowledgeRelationMapper;
    private final CourseMapper courseMapper;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 用于跟踪AI生成任务状态的内存缓存
    private final java.util.concurrent.ConcurrentHashMap<String, String> aiGenerationStatus = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.concurrent.ConcurrentHashMap<String, String> aiGenerationResults = new java.util.concurrent.ConcurrentHashMap<>();
    private final ResourceMapper resourceMapper;

    // 在构造函数中添加resourceMapper参数
    @Autowired
    public KnowledgePointServiceImpl(KnowledgePointMapper knowledgePointMapper,
                                     KnowledgeRelationMapper knowledgeRelationMapper,
                                     CourseMapper courseMapper,
                                     ResourceMapper resourceMapper,
                                     OllamaService ollamaService) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.knowledgeRelationMapper = knowledgeRelationMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
        this.ollamaService = ollamaService; // 这里初始化
    }

    @Override
    @Transactional
    public KnowledgePoint createKnowledgePoint(String courseId, KnowledgePoint knowledgePoint) {
        // 检查课程是否存在（可选）
        CourseDTO course = courseMapper.findCourseWithTeacher(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 检查知识点名称唯一性
        if (knowledgePointMapper.checkNameExists(courseId, knowledgePoint.getName(), null) > 0) {
            throw new BusinessException(409, "知识点名称已存在");
        }

        // 设置知识点ID和默认值
        knowledgePoint.setPointId(UUID.randomUUID().toString());
        knowledgePoint.setCourseId(courseId);
        knowledgePoint.setCreatedAt(new Date());
        knowledgePoint.setUpdatedAt(new Date());

        // 设置默认难度
        if (knowledgePoint.getDifficultylevel() == null) {
            knowledgePoint.setDifficultylevel(MEDIUM);
        }

        // 插入数据库
        knowledgePointMapper.insertKnowledgePoint(knowledgePoint);
        
        // 知识点创建后，检测并自动修复孤立节点
        try {
            // 异步执行检测，避免阻塞用户操作
            CompletableFuture.runAsync(() -> {
                try {
                    // 稍微延迟一下，确保事务提交完成
                    Thread.sleep(1000);
                    List<KnowledgePoint> allPoints = getKnowledgePointsByCourse(courseId, false);
                    detectAndFixIsolatedNodes(courseId, allPoints);
                    logger.info("知识点创建成功，孤立节点检测已在后台自动执行");
                } catch (Exception e) {
                    logger.error("后台检测孤立节点失败", e);
                }
            });
        } catch (Exception e) {
            logger.warn("启动后台孤立节点检测任务失败", e);
        }
        
        return knowledgePoint;
    }

    @Override
    public List<KnowledgePoint> getKnowledgePointsByCourse(String courseId, boolean includeTree) {
        // 检查课程是否存在（可选）
        CourseDTO course = courseMapper.findCourseWithTeacher(courseId);
        if (course == null) {
            throw new BusinessException(404, "课程不存在");
        }

        // 获取课程所有知识点
        List<KnowledgePoint> points = knowledgePointMapper.selectKnowledgePointsByCourseId(courseId);

        // 移除树形结构逻辑，直接返回扁平列表
        return points;
    }

    @Override
    public KnowledgePoint getKnowledgePointById(String pointId) {
        KnowledgePoint point = knowledgePointMapper.selectKnowledgePointById(pointId);
        if (point == null) {
            throw new BusinessException(404, "知识点不存在");
        }
        return point;
    }

    @Override
    @Transactional
    public KnowledgePoint updateKnowledgePoint(String pointId, KnowledgePoint knowledgePoint) {
        KnowledgePoint existingPoint = getKnowledgePointById(pointId);

        // 检查名称唯一性（排除自身）
        if (!existingPoint.getName().equals(knowledgePoint.getName())) {
            if (knowledgePointMapper.checkNameExists(
                    existingPoint.getCourseId(),
                    knowledgePoint.getName(),
                    pointId) > 0) {
                throw new BusinessException(409, "知识点名称已存在");
            }
        }

        // 更新字段
        existingPoint.setName(knowledgePoint.getName());
        existingPoint.setDescription(knowledgePoint.getDescription());
        existingPoint.setDifficultylevel(knowledgePoint.getDifficultylevel());
        existingPoint.setUpdatedAt(new Date());

        // 更新数据库
        knowledgePointMapper.updateKnowledgePoint(existingPoint);
        
        // 知识点更新后，检测并自动修复孤立节点
        try {
            String courseId = existingPoint.getCourseId();
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                    List<KnowledgePoint> allPoints = getKnowledgePointsByCourse(courseId, false);
                    detectAndFixIsolatedNodes(courseId, allPoints);
                    logger.info("知识点更新成功，孤立节点检测已在后台自动执行");
                } catch (Exception e) {
                    logger.error("后台检测孤立节点失败", e);
                }
            });
        } catch (Exception e) {
            logger.warn("启动后台孤立节点检测任务失败", e);
        }
        
        return existingPoint;
    }

    @Override
    @Transactional
    public void deleteKnowledgePoint(String pointId) {
        // 验证知识点存在并获取课程ID
        KnowledgePoint point = getKnowledgePointById(pointId);
        String courseId = point.getCourseId();

        // 检查是否关联资源
        if (knowledgePointMapper.checkHasResources(pointId) > 0) {
            throw new BusinessException(409, "知识点已关联资源，无法删除");
        }

        // 检查是否关联任务
        if (knowledgePointMapper.checkHasTasks(pointId) > 0) {
            throw new BusinessException(409, "知识点已关联任务，无法删除");
        }

        // 删除所有关联关系
        knowledgeRelationMapper.deleteRelationsByPointId(pointId);

        // 删除知识点
        knowledgePointMapper.deleteKnowledgePoint(pointId);
        
        // 知识点删除后，检测并自动修复孤立节点
        try {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1000);
                    List<KnowledgePoint> allPoints = getKnowledgePointsByCourse(courseId, false);
                    detectAndFixIsolatedNodes(courseId, allPoints);
                    logger.info("知识点删除成功，孤立节点检测已在后台自动执行");
                } catch (Exception e) {
                    logger.error("后台检测孤立节点失败", e);
                }
            });
        } catch (Exception e) {
            logger.warn("启动后台孤立节点检测任务失败", e);
        }
    }

    @Override
    @Transactional
    public void addKnowledgeRelation(String sourceId, String targetId, RelationType relationType) {
        KnowledgePoint source = getKnowledgePointById(sourceId);
        KnowledgePoint target = getKnowledgePointById(targetId);

        // 验证同一课程
        if (!source.getCourseId().equals(target.getCourseId())) {
            throw new BusinessException(400, "知识点只能关联同一课程下的其他知识点");
        }

        // 检查关系是否已存在
        if (knowledgeRelationMapper.checkRelationExists(sourceId, targetId) > 0) {
            throw new BusinessException(409, "知识点关联已存在");
        }

        // 创建新关系
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setRelationId(UUID.randomUUID().toString());
        relation.setSourcePointId(sourceId);
        relation.setTargetPointId(targetId);
        relation.setRelationType(relationType);
        relation.setCreatedAt(new Date());

        knowledgeRelationMapper.insertKnowledgeRelation(relation);
    }

    @Override
    @Transactional
    public void removeKnowledgeRelation(String relationId) {
        // 检查关系是否存在
        knowledgeRelationMapper.selectRelationsByPointId(relationId)
                .stream()
                .filter(r -> r.getRelationId().equals(relationId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(404, "知识点关系不存在"));

        // 删除关系
        knowledgeRelationMapper.deleteKnowledgeRelationById(relationId);
    }

    @Override
    public List<Object> getKnowledgePointResources(String pointId) {
        // 获取知识点关联的资源列表
        List<Resource> resources = resourceMapper.getResourcesByKnowledgePointId(pointId);

        // 转换为前端需要的格式
        List<Object> result = new ArrayList<>();
        for (Resource resource : resources) {
            Map<String, Object> resourceMap = new HashMap<>();
            resourceMap.put("resourceId", resource.getResourceId());
            resourceMap.put("name", resource.getName());
            resourceMap.put("type", resource.getType().name());
            resourceMap.put("url", resource.getUrl());
            resourceMap.put("size", resource.getSize());
            resourceMap.put("description", resource.getDescription());
            resourceMap.put("uploaderId", resource.getUploaderId());
            resourceMap.put("uploadTime", resource.getUploadTime());
            resourceMap.put("viewCount", resource.getViewCount());
            resourceMap.put("duration", resource.getDuration());
            result.add(resourceMap);
        }

        return result;
    }

    @Override
    public boolean checkCircularDependency(String sourceId, String targetId) {
        return knowledgeRelationMapper.checkCircularDependency(sourceId, targetId) > 0;
    }

    @Override
    @Transactional
    public KnowledgeGraphDTO getKnowledgeGraphByCourse(String courseId) {
        logger.info("获取课程 {} 的知识图谱", courseId);
        
        // 1. 获取课程的所有知识点
        List<KnowledgePoint> points = getKnowledgePointsByCourse(courseId, false);
        if (points.isEmpty()) {
            logger.warn("课程 {} 没有知识点", courseId);
            return new KnowledgeGraphDTO();
        }
        
        // 2. 构建节点数据
        List<KnowledgeGraphDTO.Node> nodes = points.stream().map(point -> {
            KnowledgeGraphDTO.Node node = new KnowledgeGraphDTO.Node();
            node.setId(point.getPointId());
            node.setName(point.getName());
            node.setDescription(point.getDescription());
            node.setDifficultylevel(point.getDifficultylevel() != null ? point.getDifficultylevel().name() : null);
            node.setCourseId(point.getCourseId());
            return node;
        }).collect(Collectors.toList());

        // 3. 获取现有的关系数据
        List<KnowledgeRelation> relations = knowledgeRelationMapper.selectRelationsByCourseId(courseId);
        
        // 4. 如果没有关系数据且知识点数量大于1，则调用AI生成关系
        if (relations.isEmpty() && points.size() > 1) {
            logger.info("课程 {} 没有关系数据，调用AI生成关系", courseId);
            try {
                generateKnowledgeRelationsByAI(courseId);
                // 重新获取生成的关系数据
                relations = knowledgeRelationMapper.selectRelationsByCourseId(courseId);
                logger.info("AI生成关系完成，共生成 {} 条关系", relations.size());
            } catch (Exception e) {
                logger.error("AI生成关系失败", e);
                // 即使AI生成失败，也返回节点数据
            }
        } else if (!relations.isEmpty()) {
            // 5. 如果有关系数据，检测是否存在孤立节点
            detectAndFixIsolatedNodes(courseId, points);
        }
        
        // 5. 构建边数据
        List<KnowledgeGraphDTO.Edge> edges = relations.stream().map(rel -> {
            KnowledgeGraphDTO.Edge edge = new KnowledgeGraphDTO.Edge();
            edge.setSource(rel.getSourcePointId());
            edge.setTarget(rel.getTargetPointId());
            edge.setRelationType(rel.getRelationType().name().toLowerCase());
            edge.setType(convertRelationTypeToChines(rel.getRelationType()));
            return edge;
        }).collect(Collectors.toList());

        // 6. 构建并返回知识图谱DTO
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        dto.setNodes(nodes);
        dto.setEdges(edges);
        
        logger.info("知识图谱构建完成：{} 个节点，{} 条边", nodes.size(), edges.size());
        return dto;
    }

    /**
     * 将RelationType转换为中文
     */
    private String convertRelationTypeToChines(RelationType relationType) {
        switch (relationType) {
            case PREREQUISITE:
                return "先修";
            case PART_OF:
                return "包含";
            case RELATED:
                return "相关";
            default:
                return "相关";
        }
    }

    /**
     * 基于AI生成知识点关系并保存到数据库
     * 如果知识点发生变化，会重新生成所有关系
     * 优化：使用较小的事务范围，避免长时间锁定
     */
    @Override
    public void generateKnowledgeRelationsByAI(String courseId) {
        logger.info("开始为课程 {} 生成AI知识点关系", courseId);
        
        try {
            // 1. 获取课程的所有知识点（不在事务中）
            List<KnowledgePoint> knowledgePoints = getKnowledgePointsByCourse(courseId, false);
            if (knowledgePoints.isEmpty()) {
                logger.warn("课程 {} 没有知识点，无法生成关系", courseId);
                throw new BusinessException(400, "课程中没有知识点，无法生成关系");
            }
            
            logger.info("获取到 {} 个知识点", knowledgePoints.size());
            
            // 2. 调用AI服务生成关系（不在事务中，避免长时间占用连接）
            String aiResponse = ollamaService.generateKnowledgeRelations(knowledgePoints);
            logger.info("AI生成的关系数据: {}", aiResponse);
            
            // 3. 解析AI返回的JSON数据
            JsonNode jsonNode = objectMapper.readTree(aiResponse);
            JsonNode edgesNode = jsonNode.get("edges");
            
            if (edgesNode == null || !edgesNode.isArray()) {
                logger.warn("AI返回的数据格式不正确，缺少edges数组");
                throw new BusinessException(500, "AI生成的关系数据格式不正确");
            }
            
            // 4. 准备要保存的关系数据
            List<KnowledgeRelation> relationsToSave = new ArrayList<>();
            Set<String> validPointIds = knowledgePoints.stream()
                    .map(KnowledgePoint::getPointId)
                    .collect(Collectors.toSet());
            
            logger.info("有效的知识点ID列表: {}", validPointIds);
            
            for (JsonNode edge : edgesNode) {
                try {
                    String sourceId = edge.get("source").asText();
                    String targetId = edge.get("target").asText();
                    String relationTypeStr = edge.get("relationType").asText();
                    
                    logger.info("处理关系: {} -> {} ({})", sourceId, targetId, relationTypeStr);
                    
                    // 验证知识点ID是否有效
                    if (!validPointIds.contains(sourceId) || !validPointIds.contains(targetId)) {
                        logger.warn("跳过无效的关系: {} -> {} (源ID有效: {}, 目标ID有效: {})", 
                                   sourceId, targetId, validPointIds.contains(sourceId), validPointIds.contains(targetId));
                        continue;
                    }
                    
                    // 验证关系类型
                    RelationType relationType;
                    try {
                        relationType = RelationType.valueOf(relationTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        logger.warn("跳过无效的关系类型: {}", relationTypeStr);
                        continue;
                    }
                    
                    // 创建关系对象
                    KnowledgeRelation relation = new KnowledgeRelation();
                    relation.setRelationId(UUID.randomUUID().toString());
                    relation.setSourcePointId(sourceId);
                    relation.setTargetPointId(targetId);
                    relation.setRelationType(relationType);
                    relation.setCreatedAt(new Date());
                    
                    relationsToSave.add(relation);
                    
                } catch (Exception e) {
                    logger.warn("处理关系时出错: {}", e.getMessage());
                }
            }
            
            // 5. 在短事务中批量保存关系
            saveRelationsInTransaction(courseId, relationsToSave);
            
            // 6. 检测并自动修复孤立节点
            detectAndFixIsolatedNodes(courseId, knowledgePoints);
            
            logger.info("成功为课程 {} 生成并保存了 {} 个知识点关系", courseId, relationsToSave.size());
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("生成知识点关系时发生错误", e);
            throw new BusinessException(500, "生成知识点关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 在短事务中批量保存关系数据
     */
    @Transactional
    protected void saveRelationsInTransaction(String courseId, List<KnowledgeRelation> relationsToSave) {
        try {
            // 1. 删除该课程现有的所有知识点关系（重新生成）
            knowledgeRelationMapper.deleteRelationsByCourseId(courseId);
            logger.info("已删除课程 {} 的现有关系", courseId);
            
            // 2. 批量保存新关系
            int savedCount = 0;
            for (KnowledgeRelation relation : relationsToSave) {
                try {
                    // 检查关系是否已存在（避免重复）
                    if (knowledgeRelationMapper.checkRelationExists(
                            relation.getSourcePointId(), relation.getTargetPointId()) == 0) {
                        knowledgeRelationMapper.insertKnowledgeRelation(relation);
                        savedCount++;
                        logger.debug("保存关系: {} -> {} ({})", 
                                   relation.getSourcePointId(), 
                                   relation.getTargetPointId(), 
                                   relation.getRelationType());
                    }
                } catch (Exception e) {
                    logger.warn("保存单个关系时出错: {}", e.getMessage());
                    // 继续处理其他关系，不中断整个过程
                }
            }
            
            logger.info("实际保存了 {} 个关系", savedCount);
            
        } catch (Exception e) {
            logger.error("批量保存关系时发生错误", e);
            throw new BusinessException(500, "保存关系失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查课程知识点是否发生变化，如果变化则重新生成关系
     * 优化：添加并发控制，避免同时执行
     */
    @Override
    public void updateKnowledgeRelationsIfChanged(String courseId) {
        logger.info("检查课程 {} 的知识点变化", courseId);
        
        // 简单的并发控制：使用courseId作为锁
        synchronized (("update_relations_" + courseId).intern()) {
            try {
                // 获取当前知识点
                List<KnowledgePoint> currentPoints = getKnowledgePointsByCourse(courseId, false);
                
                // 获取现有关系
                List<KnowledgeRelation> existingRelations = knowledgeRelationMapper.selectRelationsByCourseId(courseId);
                
                // 检查是否需要重新生成关系
                boolean needRegenerate = shouldRegenerateRelations(currentPoints, existingRelations);
                
                if (needRegenerate) {
                    logger.info("检测到知识点变化，重新生成关系");
                    generateKnowledgeRelationsByAI(courseId);
                } else {
                    logger.info("知识点无变化，无需重新生成关系");
                }
                
            } catch (Exception e) {
                logger.error("检查知识点变化时发生错误", e);
                throw new BusinessException(500, "检查知识点变化失败: " + e.getMessage());
            }
        }
    }
    
    /**
     * 判断是否需要重新生成关系
     */
    private boolean shouldRegenerateRelations(List<KnowledgePoint> currentPoints, 
                                            List<KnowledgeRelation> existingRelations) {
        // 如果没有现有关系，需要生成
        if (existingRelations.isEmpty()) {
            return !currentPoints.isEmpty();
        }
        
        // 如果知识点数量少于关系中涉及的节点数，说明有知识点被删除
        Set<String> relationPointIds = new HashSet<>();
        for (KnowledgeRelation relation : existingRelations) {
            relationPointIds.add(relation.getSourcePointId());
            relationPointIds.add(relation.getTargetPointId());
        }
        
        Set<String> currentPointIds = currentPoints.stream()
                .map(KnowledgePoint::getPointId)
                .collect(Collectors.toSet());
        
        // 如果关系中的知识点ID与当前知识点ID不匹配，说明有变化
        return !currentPointIds.equals(relationPointIds);
    }

    /**
     * 异步生成知识点关系
     */
    @Async
    public CompletableFuture<Void> generateKnowledgeRelationsByAIAsync(String courseId) {
        return CompletableFuture.runAsync(() -> {
            try {
                aiGenerationStatus.put(courseId, "PROCESSING");
                generateKnowledgeRelationsByAI(courseId);
                aiGenerationStatus.put(courseId, "COMPLETED");
                aiGenerationResults.put(courseId, "AI生成知识点关系成功");
            } catch (Exception e) {
                aiGenerationStatus.put(courseId, "FAILED");
                aiGenerationResults.put(courseId, "生成失败: " + e.getMessage());
                logger.error("异步生成知识点关系失败", e);
            }
        });
    }

    /**
     * 获取AI生成任务状态
     */
    public Map<String, Object> getAIGenerationStatus(String courseId) {
        Map<String, Object> result = new HashMap<>();
        String status = aiGenerationStatus.getOrDefault(courseId, "NOT_STARTED");
        String message = aiGenerationResults.getOrDefault(courseId, "");
        
        result.put("status", status);
        result.put("message", message);
        result.put("courseId", courseId);
        
        return result;
    }

    /**
     * 清理任务状态缓存
     */
    public void clearAIGenerationStatus(String courseId) {
        aiGenerationStatus.remove(courseId);
        aiGenerationResults.remove(courseId);
    }

    /**
     * 检测并自动修复孤立节点
     */
    private void detectAndFixIsolatedNodes(String courseId, List<KnowledgePoint> knowledgePoints) {
        try {
            // 获取现有关系
            List<KnowledgeRelation> existingRelations = knowledgeRelationMapper.selectRelationsByCourseId(courseId);
            
            // 找出已连接的知识点ID
            Set<String> connectedPointIds = new HashSet<>();
            for (KnowledgeRelation relation : existingRelations) {
                connectedPointIds.add(relation.getSourcePointId());
                connectedPointIds.add(relation.getTargetPointId());
            }
            
            // 找出孤立的知识点
            List<KnowledgePoint> isolatedPoints = new ArrayList<>();
            for (KnowledgePoint point : knowledgePoints) {
                if (!connectedPointIds.contains(point.getPointId())) {
                    isolatedPoints.add(point);
                }
            }
            
            logger.info("检测到 {} 个孤立的知识点: {}", isolatedPoints.size(), 
                       isolatedPoints.stream().map(KnowledgePoint::getName).collect(Collectors.toList()));
            
            // 如果有孤立节点且知识点总数大于1，则自动调用AI重新生成关系
            if (!isolatedPoints.isEmpty() && knowledgePoints.size() > 1) {
                logger.info("发现孤立节点，自动调用AI重新生成关系");
                
                // 异步调用AI重新生成关系，避免阻塞当前操作
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(2000); // 等待2秒确保当前事务提交
                        logger.info("开始自动重新生成知识点关系...");
                        generateKnowledgeRelationsByAI(courseId);
                        logger.info("自动重新生成关系完成");
                    } catch (Exception e) {
                        logger.error("自动重新生成关系失败", e);
                    }
                });
            } else if (isolatedPoints.isEmpty()) {
                logger.info("所有知识点都已连接，无需重新生成关系");
            }
            
        } catch (Exception e) {
            logger.error("检测孤立节点时发生错误", e);
        }
    }
}
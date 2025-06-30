package com.sx.backend.service;

import java.util.List;

import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.entity.RelationType;

public interface KnowledgePointService {
    /**
     * 创建知识点
     * @param courseId 课程ID
     * @param knowledgePoint 知识点数据
     * @return 创建的知识点
     */
    KnowledgePoint createKnowledgePoint(String courseId, KnowledgePoint knowledgePoint);

    /**
     * 获取课程知识点列表
     * @param courseId 课程ID
     * @param includeTree 是否返回树形结构
     * @return 知识点列表
     */
    List<KnowledgePoint> getKnowledgePointsByCourse(String courseId, boolean includeTree);

    /**
     * 获取知识点详情
     * @param pointId 知识点ID
     * @return 知识点详情
     */
    KnowledgePoint getKnowledgePointById(String pointId);

    /**
     * 更新知识点
     * @param pointId 知识点ID
     * @param knowledgePoint 更新数据
     * @return 更新后的知识点
     */
    KnowledgePoint updateKnowledgePoint(String pointId, KnowledgePoint knowledgePoint);

    /**
     * 删除知识点
     * @param pointId 知识点ID
     */
    void deleteKnowledgePoint(String pointId);

    /**
     * 管理知识点关系
     * @param sourceId 源知识点ID
     * @param targetId 目标知识点ID
     * @param relationType 关系类型
     */
    void addKnowledgeRelation(String sourceId, String targetId, RelationType relationType);

    /**
     * 删除知识点关系
     * @param relationId 关系ID
     */
    void removeKnowledgeRelation(String relationId);

    /**
     * 获取知识点关联的资源
     * @param pointId 知识点ID
     * @return 关联的资源列表
     */
    List<Object> getKnowledgePointResources(String pointId);

    /**
     * 检查知识点是否存在循环依赖
     * @param sourceId 源知识点ID
     * @param targetId 目标知识点ID
     * @return 是否存在循环依赖
     */
    boolean checkCircularDependency(String sourceId, String targetId);

    /**
     * 获取知识点的关系图数据
     * @param courseId 课程ID
     * @return 知识点关系图数据
     */
    KnowledgeGraphDTO getKnowledgeGraphByCourse(String courseId);

    /**
     * 基于AI生成知识点关系并保存到数据库
     * @param courseId 课程ID
     */
    void generateKnowledgeRelationsByAI(String courseId);

    /**
     * 检查课程知识点是否发生变化，如果变化则重新生成关系
     * @param courseId 课程ID
     */
    void updateKnowledgeRelationsIfChanged(String courseId);

    /**
     * 异步生成知识点关系
     * @param courseId 课程ID
     * @return CompletableFuture
     */
    java.util.concurrent.CompletableFuture<Void> generateKnowledgeRelationsByAIAsync(String courseId);

    /**
     * 获取AI生成任务状态
     * @param courseId 课程ID
     * @return 任务状态信息
     */
    java.util.Map<String, Object> getAIGenerationStatus(String courseId);

    /**
     * 清理AI生成任务状态
     * @param courseId 课程ID
     */
    void clearAIGenerationStatus(String courseId);
}
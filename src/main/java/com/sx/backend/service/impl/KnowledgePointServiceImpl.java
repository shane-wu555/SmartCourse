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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.sx.backend.entity.DifficultyLevel.MEDIUM;

@Service
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final KnowledgeRelationMapper knowledgeRelationMapper;
    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;

    // 在构造函数中添加resourceMapper参数
    @Autowired
    public KnowledgePointServiceImpl(KnowledgePointMapper knowledgePointMapper,
                                     KnowledgeRelationMapper knowledgeRelationMapper,
                                     CourseMapper courseMapper,
                                     ResourceMapper resourceMapper) {
        this.knowledgePointMapper = knowledgePointMapper;
        this.knowledgeRelationMapper = knowledgeRelationMapper;
        this.courseMapper = courseMapper;
        this.resourceMapper = resourceMapper;
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
        if (knowledgePoint.getDifficulty() == null) {
            knowledgePoint.setDifficulty(MEDIUM);
        }

        // 验证父知识点
        if (knowledgePoint.getParentId() != null) {
            KnowledgePoint parent = getKnowledgePointById(knowledgePoint.getParentId());
            if (!parent.getCourseId().equals(courseId)) {
                throw new BusinessException(400, "父知识点不属于该课程");
            }

            // 检查层级深度
            int depth = calculateDepth(parent);
            if (depth >= 5) {
                throw new BusinessException(400, "知识点层级深度不能超过5级");
            }
        }

        // 插入数据库
        knowledgePointMapper.insertKnowledgePoint(knowledgePoint);
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

        if (includeTree) {
            return buildKnowledgeTree(points);
        }
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
        existingPoint.setDifficulty(knowledgePoint.getDifficulty());
        existingPoint.setUpdatedAt(new Date());

        // 更新数据库
        knowledgePointMapper.updateKnowledgePoint(existingPoint);
        return existingPoint;
    }

    @Override
    @Transactional
    public void deleteKnowledgePoint(String pointId) {
        KnowledgePoint point = getKnowledgePointById(pointId);

        // 检查是否有子节点
        if (knowledgePointMapper.checkHasChildren(pointId) > 0) {
            throw new BusinessException(409, "知识点包含子节点，无法删除");
        }

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
    }

    @Override
    @Transactional
    public KnowledgePoint updateKnowledgePointParent(String pointId, String parentId) {
        KnowledgePoint point = getKnowledgePointById(pointId);

        // 如果parentId为空，表示设为根节点
        if (parentId == null || parentId.isEmpty()) {
            point.setParentId(null);
            knowledgePointMapper.updatePointParent(pointId, null);
            return point;
        }

        // 验证新父节点
        KnowledgePoint newParent = getKnowledgePointById(parentId);

        // 验证同一课程
        if (!point.getCourseId().equals(newParent.getCourseId())) {
            throw new BusinessException(400, "知识点和父节点不属于同一课程");
        }

        // 防止循环依赖
        if (checkCircularDependency(parentId, pointId)) {
            throw new BusinessException(409, "设置父节点会导致循环依赖");
        }

        // 检查层级深度
        int depth = calculateDepth(newParent);
        if (depth >= 5) {
            throw new BusinessException(400, "知识点层级深度不能超过5级");
        }

        // 更新父节点
        point.setParentId(parentId);
        knowledgePointMapper.updatePointParent(pointId, parentId);
        return point;
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

        // 检查循环依赖（针对依赖关系）
        if (relationType == RelationType.DEPENDENCY) {
            if (checkCircularDependency(targetId, sourceId)) {
                throw new BusinessException(409, "添加依赖关系会导致循环依赖");
            }
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
        KnowledgeRelation relation = knowledgeRelationMapper.selectRelationsByPointId(relationId)
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

    // 构建知识点树形结构
    private List<KnowledgePoint> buildKnowledgeTree(List<KnowledgePoint> points) {
        // 按ID分组
        Map<String, KnowledgePoint> pointMap = new HashMap<>();
        for (KnowledgePoint point : points) {
            pointMap.put(point.getPointId(), point);
            point.setChildren(new ArrayList<>());
        }

        // 构建树结构
        List<KnowledgePoint> rootPoints = new ArrayList<>();
        for (KnowledgePoint point : points) {
            if (point.getParentId() == null || point.getParentId().isEmpty()) {
                rootPoints.add(point);
            } else {
                KnowledgePoint parent = pointMap.get(point.getParentId());
                if (parent != null) {
                    parent.getChildren().add(point);
                }
            }
        }

        // 对根节点排序
        rootPoints.sort(Comparator.comparing(KnowledgePoint::getName));

        // 递归排序子节点
        for (KnowledgePoint root : rootPoints) {
            sortChildren(root);
        }

        return rootPoints;
    }

    // 递归排序子节点
    private void sortChildren(KnowledgePoint point) {
        if (point.getChildren() != null && !point.getChildren().isEmpty()) {
            point.getChildren().sort(Comparator.comparing(KnowledgePoint::getName));
            for (KnowledgePoint child : point.getChildren()) {
                sortChildren(child);
            }
        }
    }

    // 计算知识点深度
    private int calculateDepth(KnowledgePoint point) {
        int depth = 1;
        if (point.getParentId() != null) {
            KnowledgePoint parent = knowledgePointMapper.selectKnowledgePointById(point.getParentId());
            if (parent != null) {
                depth += calculateDepth(parent);
            }
        }
        return depth;
    }
}
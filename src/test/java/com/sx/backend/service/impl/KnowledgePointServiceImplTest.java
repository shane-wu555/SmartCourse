/*package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.entity.*;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class KnowledgePointServiceImplTest {

    @Mock
    private KnowledgePointMapper knowledgePointMapper;

    @Mock
    private KnowledgeRelationMapper knowledgeRelationMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ResourceMapper resourceMapper;

    @InjectMocks
    private KnowledgePointServiceImpl knowledgePointService;

    private static final String COURSE_ID = "course-1";
    private static final String POINT_ID = "point-1";
    private static final String PARENT_ID = "parent-1";
    private KnowledgePoint knowledgePoint;
    private CourseDTO course;

    @BeforeEach
    void setUp() {
        course = new CourseDTO();
        course.setCourseId(COURSE_ID);

        knowledgePoint = new KnowledgePoint();
        knowledgePoint.setPointId(POINT_ID);
        knowledgePoint.setName("Test Point");
        knowledgePoint.setCourseId(COURSE_ID);
        knowledgePoint.setParentId(PARENT_ID);
    }

    @Test
    void createKnowledgePoint_Success() {
        // 准备数据
        KnowledgePoint newPoint = new KnowledgePoint();
        newPoint.setName("New Point");
        newPoint.setParentId(PARENT_ID);

        // 模拟行为
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(course);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, newPoint.getName(), null)).thenReturn(0);
        when(knowledgePointMapper.selectKnowledgePointById(PARENT_ID)).thenReturn(knowledgePoint);
        when(knowledgePointMapper.insertKnowledgePoint(any(KnowledgePoint.class))).thenReturn(1);

        // 执行方法
        KnowledgePoint result = knowledgePointService.createKnowledgePoint(COURSE_ID, newPoint);

        // 验证结果
        assertNotNull(result.getPointId());
        assertEquals(COURSE_ID, result.getCourseId());
        assertEquals(DifficultyLevel.MEDIUM, result.getDifficultylevel());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void createKnowledgePoint_NameConflict() {
        // 模拟行为
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(course);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, knowledgePoint.getName(), null)).thenReturn(1);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> knowledgePointService.createKnowledgePoint(COURSE_ID, knowledgePoint));

        assertEquals(409, exception.getCode());
        assertEquals("知识点名称已存在", exception.getMessage());
    }

    @Test
    void getKnowledgePointsByCourse_WithTree() {
        // 准备数据
        KnowledgePoint childPoint = new KnowledgePoint();
        childPoint.setPointId("point-2");
        childPoint.setParentId(POINT_ID);

        List<KnowledgePoint> points = Arrays.asList(knowledgePoint, childPoint);

        // 模拟行为
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(course);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);

        // 执行方法
        List<KnowledgePoint> result = knowledgePointService.getKnowledgePointsByCourse(COURSE_ID, true);

        // 验证结果
        assertEquals(1, result.size()); // 只应返回根节点
        assertEquals(1, result.get(0).getChildren().size()); // 根节点应包含一个子节点
    }

    @Test
    void updateKnowledgePoint_Success() {
        // 准备数据
        KnowledgePoint updateData = new KnowledgePoint();
        updateData.setName("Updated Name");
        updateData.setDescription("Updated Description");

        // 模拟行为
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(knowledgePoint);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, updateData.getName(), POINT_ID)).thenReturn(0);
        when(knowledgePointMapper.updateKnowledgePoint(any(KnowledgePoint.class))).thenReturn(1);

        // 执行方法
        KnowledgePoint result = knowledgePointService.updateKnowledgePoint(POINT_ID, updateData);

        // 验证结果
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void deleteKnowledgePoint_HasResources() {
        // 模拟行为
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(knowledgePoint);
        when(knowledgePointMapper.checkHasChildren(POINT_ID)).thenReturn(0);
        when(knowledgePointMapper.checkHasResources(POINT_ID)).thenReturn(1); // 存在资源

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> knowledgePointService.deleteKnowledgePoint(POINT_ID));

        assertEquals(409, exception.getCode());
        assertEquals("知识点已关联资源，无法删除", exception.getMessage());
    }

    @Test
    void updateKnowledgePointParent_CircularDependency() {
        // 准备数据
        KnowledgePoint childPoint = new KnowledgePoint();
        childPoint.setPointId("child-1");
        childPoint.setParentId(POINT_ID);

        // 模拟行为
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(knowledgePoint);
        when(knowledgePointMapper.selectKnowledgePointById("child-1")).thenReturn(childPoint);
        when(knowledgeRelationMapper.checkCircularDependency(eq(POINT_ID), eq("child-1"))).thenReturn(1);

        // 验证异常
        BusinessException exception = assertThrows(BusinessException.class,
                () -> knowledgePointService.updateKnowledgePointParent("child-1", POINT_ID));

        assertEquals(409, exception.getCode());
        assertEquals("设置父节点会导致循环依赖", exception.getMessage());
    }

    @Test
    void addKnowledgeRelation_Success() {
        // 准备数据
        String targetId = "point-2";
        KnowledgePoint target = new KnowledgePoint();
        target.setPointId(targetId);
        target.setCourseId(COURSE_ID);

        // 模拟行为
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(knowledgePoint);
        when(knowledgePointMapper.selectKnowledgePointById(targetId)).thenReturn(target);
        when(knowledgeRelationMapper.checkRelationExists(POINT_ID, targetId)).thenReturn(0);
        when(knowledgeRelationMapper.checkCircularDependency(eq(targetId), eq(POINT_ID))).thenReturn(0);
        when(knowledgeRelationMapper.insertKnowledgeRelation(any(KnowledgeRelation.class))).thenReturn(1);

        // 执行方法
        knowledgePointService.addKnowledgeRelation(POINT_ID, targetId, RelationType.RELATED);

        // 验证行为
        verify(knowledgeRelationMapper).insertKnowledgeRelation(any(KnowledgeRelation.class));
    }

    @Test
    void getKnowledgeGraphByCourse_Success() {
        // 准备数据
        KnowledgePoint point1 = new KnowledgePoint();
        point1.setPointId(POINT_ID);

        KnowledgePoint point2 = new KnowledgePoint();
        point2.setPointId("point-2");

        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setSourcePointId(POINT_ID);
        relation.setTargetPointId("point-2");
        relation.setRelationType(RelationType.DEPENDENCY);

        // 模拟行为
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(course);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID))
                .thenReturn(Arrays.asList(point1, point2));
        when(knowledgeRelationMapper.selectRelationsByCourseId(COURSE_ID))
                .thenReturn(Collections.singletonList(relation));

        // 执行方法
        KnowledgeGraphDTO result = knowledgePointService.getKnowledgeGraphByCourse(COURSE_ID);

        // 验证结果
        assertEquals(2, result.getNodes().size());
        assertEquals(1, result.getEdges().size());
        assertEquals(RelationType.DEPENDENCY.name(), result.getEdges().get(0).getType());
    }

    @Test
    void getKnowledgePointResources_Success() {
        // 准备数据
        Resource resource = new Resource();
        resource.setResourceId("res-1");
        resource.setName("Resource 1");

        // 模拟行为
        when(resourceMapper.getResourcesByKnowledgePointId(POINT_ID))
                .thenReturn(Collections.singletonList(resource));

        // 执行方法
        List<Object> result = knowledgePointService.getKnowledgePointResources(POINT_ID);

        // 验证结果
        assertEquals(1, result.size());
        Map<String, Object> resourceMap = (Map<String, Object>) result.get(0);
        assertEquals("res-1", resourceMap.get("resourceId"));
        assertEquals("Resource 1", resourceMap.get("name"));
    }
}*/

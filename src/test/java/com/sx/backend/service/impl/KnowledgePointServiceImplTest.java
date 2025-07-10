package com.sx.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sx.backend.dto.CourseDTO;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.entity.*;
import com.sx.backend.exception.BusinessException;
import com.sx.backend.mapper.*;
import com.sx.backend.service.OllamaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class KnowledgePointServiceImplTest {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgePointServiceImplTest.class);

    @Mock private KnowledgePointMapper knowledgePointMapper;
    @Mock private KnowledgeRelationMapper knowledgeRelationMapper;
    @Mock private CourseMapper courseMapper;
    @Mock private ResourceMapper resourceMapper;
    @Mock private OllamaService ollamaService;

    @InjectMocks
    private KnowledgePointServiceImpl knowledgePointService;

    private final String COURSE_ID = "course-123";
    private final String POINT_ID = "point-456";
    private final String RELATION_ID = "rel-789";
    private KnowledgePoint testKnowledgePoint;
    private CourseDTO testCourse;

    @BeforeEach
    void setUp() {
        testCourse = new CourseDTO();
        testCourse.setCourseId(COURSE_ID);

        testKnowledgePoint = new KnowledgePoint();
        testKnowledgePoint.setPointId(POINT_ID);
        testKnowledgePoint.setCourseId(COURSE_ID);
        testKnowledgePoint.setName("Test Point");
        testKnowledgePoint.setDescription("Test Description");
        testKnowledgePoint.setDifficultylevel(DifficultyLevel.MEDIUM);
    }

    // ========== 知识点创建测试 ==========
    @Test
    void createKnowledgePoint_Success() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, "New Point", null)).thenReturn(0);

        KnowledgePoint newPoint = new KnowledgePoint();
        newPoint.setName("New Point");
        KnowledgePoint result = knowledgePointService.createKnowledgePoint(COURSE_ID, newPoint);

        assertNotNull(result.getPointId());
        assertEquals(COURSE_ID, result.getCourseId());
        assertEquals(DifficultyLevel.MEDIUM, result.getDifficultylevel());
        verify(knowledgePointMapper).insertKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void createKnowledgePoint_CourseNotFound() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.createKnowledgePoint(COURSE_ID, testKnowledgePoint));

        assertEquals(404, ex.getCode());
        assertEquals("课程不存在", ex.getMessage());
    }

    @Test
    void createKnowledgePoint_NameConflict() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, "Existing Point", null)).thenReturn(1);

        KnowledgePoint existingPoint = new KnowledgePoint();
        existingPoint.setName("Existing Point");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.createKnowledgePoint(COURSE_ID, existingPoint));

        assertEquals(409, ex.getCode());
        assertEquals("知识点名称已存在", ex.getMessage());
    }

    // ========== 知识点查询测试 ==========
    @Test
    void getKnowledgePointsByCourse_Success() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID))
                .thenReturn(Arrays.asList(testKnowledgePoint));

        List<KnowledgePoint> result = knowledgePointService.getKnowledgePointsByCourse(COURSE_ID, false);

        assertEquals(1, result.size());
        assertEquals(POINT_ID, result.get(0).getPointId());
    }

    @Test
    void getKnowledgePointsByCourse_CourseNotFound() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.getKnowledgePointsByCourse(COURSE_ID, false));

        assertEquals(404, ex.getCode());
        assertEquals("课程不存在", ex.getMessage());
    }

    @Test
    void getKnowledgePointById_Success() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(testKnowledgePoint);

        KnowledgePoint result = knowledgePointService.getKnowledgePointById(POINT_ID);
        assertEquals(POINT_ID, result.getPointId());
    }

    @Test
    void getKnowledgePointById_NotFound() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.getKnowledgePointById(POINT_ID));

        assertEquals(404, ex.getCode());
        assertEquals("知识点不存在", ex.getMessage());
    }

    // ========== 知识点更新测试 ==========
    @Test
    void updateKnowledgePoint_Success() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(testKnowledgePoint);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, "Updated Point", POINT_ID)).thenReturn(0);

        KnowledgePoint update = new KnowledgePoint();
        update.setName("Updated Point");
        update.setDescription("Updated Description");

        KnowledgePoint result = knowledgePointService.updateKnowledgePoint(POINT_ID, update);

        assertEquals("Updated Point", result.getName());
        assertEquals("Updated Description", result.getDescription());
        verify(knowledgePointMapper).updateKnowledgePoint(any(KnowledgePoint.class));
    }

    @Test
    void updateKnowledgePoint_NameConflict() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(testKnowledgePoint);
        when(knowledgePointMapper.checkNameExists(COURSE_ID, "Existing Point", POINT_ID)).thenReturn(1);

        KnowledgePoint update = new KnowledgePoint();
        update.setName("Existing Point");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.updateKnowledgePoint(POINT_ID, update));

        assertEquals(409, ex.getCode());
        assertEquals("知识点名称已存在", ex.getMessage());
    }

    // ========== 知识点删除测试 ==========
    @Test
    void deleteKnowledgePoint_Success() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(testKnowledgePoint);
        when(knowledgePointMapper.checkHasResources(POINT_ID)).thenReturn(0);
        when(knowledgePointMapper.checkHasTasks(POINT_ID)).thenReturn(0);

        knowledgePointService.deleteKnowledgePoint(POINT_ID);

        verify(knowledgeRelationMapper).deleteRelationsByPointId(POINT_ID);
        verify(knowledgePointMapper).deleteKnowledgePoint(POINT_ID);
    }

    @Test
    void deleteKnowledgePoint_WithResources() {
        when(knowledgePointMapper.selectKnowledgePointById(POINT_ID)).thenReturn(testKnowledgePoint);
        when(knowledgePointMapper.checkHasResources(POINT_ID)).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.deleteKnowledgePoint(POINT_ID));

        assertEquals(409, ex.getCode());
        assertEquals("知识点已关联资源，无法删除", ex.getMessage());
    }

    // ========== 知识点关系测试 ==========
    @Test
    void addKnowledgeRelation_Success() {
        KnowledgePoint source = new KnowledgePoint();
        source.setPointId("source-1");
        source.setCourseId(COURSE_ID);

        KnowledgePoint target = new KnowledgePoint();
        target.setPointId("target-1");
        target.setCourseId(COURSE_ID);

        when(knowledgePointMapper.selectKnowledgePointById("source-1")).thenReturn(source);
        when(knowledgePointMapper.selectKnowledgePointById("target-1")).thenReturn(target);
        when(knowledgeRelationMapper.checkRelationExists("source-1", "target-1")).thenReturn(0);

        knowledgePointService.addKnowledgeRelation("source-1", "target-1", RelationType.PREREQUISITE);

        verify(knowledgeRelationMapper).insertKnowledgeRelation(any(KnowledgeRelation.class));
    }

    @Test
    void addKnowledgeRelation_DifferentCourses() {
        KnowledgePoint source = new KnowledgePoint();
        source.setPointId("source-1");
        source.setCourseId("course-1");

        KnowledgePoint target = new KnowledgePoint();
        target.setPointId("target-1");
        target.setCourseId("course-2");

        when(knowledgePointMapper.selectKnowledgePointById("source-1")).thenReturn(source);
        when(knowledgePointMapper.selectKnowledgePointById("target-1")).thenReturn(target);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.addKnowledgeRelation("source-1", "target-1", RelationType.PREREQUISITE));

        assertEquals(400, ex.getCode());
        assertEquals("知识点只能关联同一课程下的其他知识点", ex.getMessage());
    }

    @Test
    void addKnowledgeRelation_AlreadyExists() {
        KnowledgePoint source = new KnowledgePoint();
        source.setPointId("source-1");
        source.setCourseId(COURSE_ID);

        KnowledgePoint target = new KnowledgePoint();
        target.setPointId("target-1");
        target.setCourseId(COURSE_ID);

        when(knowledgePointMapper.selectKnowledgePointById("source-1")).thenReturn(source);
        when(knowledgePointMapper.selectKnowledgePointById("target-1")).thenReturn(target);
        when(knowledgeRelationMapper.checkRelationExists("source-1", "target-1")).thenReturn(1);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.addKnowledgeRelation("source-1", "target-1", RelationType.PREREQUISITE));

        assertEquals(409, ex.getCode());
        assertEquals("知识点关联已存在", ex.getMessage());
    }

    @Test
    void removeKnowledgeRelation_Success() {
        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setRelationId(RELATION_ID);

        // 修复：使用 anyString() 匹配任何参数
        when(knowledgeRelationMapper.selectRelationsByPointId(anyString()))
                .thenReturn(Collections.singletonList(relation));

        knowledgePointService.removeKnowledgeRelation(RELATION_ID);
        verify(knowledgeRelationMapper).deleteKnowledgeRelationById(RELATION_ID);
    }

    @Test
    void removeKnowledgeRelation_NotFound() {
        // 修复：使用 anyString() 匹配任何参数
        when(knowledgeRelationMapper.selectRelationsByPointId(anyString()))
                .thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.removeKnowledgeRelation(RELATION_ID));

        assertEquals(404, ex.getCode());
        assertEquals("知识点关系不存在", ex.getMessage());
    }

    // ========== 知识图谱测试 ==========
    @Test
    void getKnowledgeGraphByCourse_NoPoints() {
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID))
                .thenReturn(Collections.emptyList());

        KnowledgeGraphDTO result = knowledgePointService.getKnowledgeGraphByCourse(COURSE_ID);

        // 修复NPE问题：直接检查是否为null而不是调用isEmpty()
        assertNull(result.getNodes());
        assertNull(result.getEdges());
    }

    @Test
    void getKnowledgeGraphByCourse_WithExistingRelations() {
        // 准备测试数据
        List<KnowledgePoint> points = Arrays.asList(
                createPoint("p1", "Point 1"),
                createPoint("p2", "Point 2")
        );

        KnowledgeRelation relation = new KnowledgeRelation();
        relation.setSourcePointId("p1");
        relation.setTargetPointId("p2");
        relation.setRelationType(RelationType.PREREQUISITE);

        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);
        when(knowledgeRelationMapper.selectRelationsByCourseId(COURSE_ID)).thenReturn(Collections.singletonList(relation));

        KnowledgeGraphDTO result = knowledgePointService.getKnowledgeGraphByCourse(COURSE_ID);

        assertNotNull(result.getNodes());
        assertEquals(2, result.getNodes().size());

        assertNotNull(result.getEdges());
        assertEquals(1, result.getEdges().size());
        assertEquals("p1", result.getEdges().get(0).getSource());
        assertEquals("p2", result.getEdges().get(0).getTarget());
    }

    @Test
    void getKnowledgeGraphByCourse_WithAIGeneration() throws Exception {
        // 准备测试数据
        List<KnowledgePoint> points = Arrays.asList(
                createPoint("p1", "Point 1"),
                createPoint("p2", "Point 2")
        );

        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);
        when(knowledgeRelationMapper.selectRelationsByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        // 创建模拟的AI响应
        String aiResponse = createMockAIResponse();
        when(ollamaService.generateKnowledgeRelations(anyList())).thenReturn(aiResponse);

        KnowledgeGraphDTO result = knowledgePointService.getKnowledgeGraphByCourse(COURSE_ID);

        assertNotNull(result.getNodes());
        assertEquals(2, result.getNodes().size());

        assertNotNull(result.getEdges());
        assertEquals(1, result.getEdges().size());
    }

    // ========== AI关系生成测试 ==========
    @Test
    void generateKnowledgeRelationsByAI_Success() throws Exception {
        // 准备测试数据
        List<KnowledgePoint> points = Arrays.asList(
                createPoint("p1", "Point 1"),
                createPoint("p2", "Point 2")
        );

        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        // 模拟获取知识点列表
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);

        // 创建模拟的AI响应
        String aiResponse = createMockAIResponse();
        when(ollamaService.generateKnowledgeRelations(points)).thenReturn(aiResponse);

        // 执行测试
        knowledgePointService.generateKnowledgeRelationsByAI(COURSE_ID);

        // 验证
        verify(knowledgeRelationMapper).deleteRelationsByCourseId(COURSE_ID);
        verify(knowledgeRelationMapper).insertKnowledgeRelation(any(KnowledgeRelation.class));
    }

    @Test
    void generateKnowledgeRelationsByAI_NoPoints() {
        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        // 模拟没有知识点
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.generateKnowledgeRelationsByAI(COURSE_ID));

        assertEquals(400, ex.getCode());
        assertEquals("课程中没有知识点，无法生成关系", ex.getMessage());
    }

    @Test
    void generateKnowledgeRelationsByAI_InvalidResponse() throws Exception {
        // 准备测试数据
        List<KnowledgePoint> points = Arrays.asList(
                createPoint("p1", "Point 1"),
                createPoint("p2", "Point 2")
        );

        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        // 模拟获取知识点列表
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);

        // 无效的AI响应
        when(ollamaService.generateKnowledgeRelations(points)).thenReturn("invalid json");

        BusinessException ex = assertThrows(BusinessException.class, () ->
                knowledgePointService.generateKnowledgeRelationsByAI(COURSE_ID));

        assertEquals(500, ex.getCode());
        assertTrue(ex.getMessage().contains("解析AI返回的JSON数据失败"));
    }

    // ========== 异步任务测试 ==========
    @Test
    void generateKnowledgeRelationsByAIAsync_Success() throws Exception {
        // 准备测试数据
        List<KnowledgePoint> points = Arrays.asList(
                createPoint("p1", "Point 1"),
                createPoint("p2", "Point 2")
        );

        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        // 模拟获取知识点列表
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);

        // 创建模拟的AI响应
        String aiResponse = createMockAIResponse();
        when(ollamaService.generateKnowledgeRelations(points)).thenReturn(aiResponse);

        // 执行异步任务
        knowledgePointService.generateKnowledgeRelationsByAIAsync(COURSE_ID).get();

        // 验证状态
        Map<String, Object> status = knowledgePointService.getAIGenerationStatus(COURSE_ID);
        assertEquals("COMPLETED", status.get("status"));
        assertEquals("AI生成知识点关系成功", status.get("message"));
    }

    @Test
    void getAIGenerationStatus_NotStarted() {
        Map<String, Object> status = knowledgePointService.getAIGenerationStatus(COURSE_ID);
        assertEquals("NOT_STARTED", status.get("status"));
    }

    @Test
    void clearAIGenerationStatus() {
        knowledgePointService.getAIGenerationStatus(COURSE_ID); // 初始化状态
        knowledgePointService.clearAIGenerationStatus(COURSE_ID);

        Map<String, Object> status = knowledgePointService.getAIGenerationStatus(COURSE_ID);
        assertEquals("NOT_STARTED", status.get("status"));
    }

    // ========== 孤立节点检测测试 ==========
    @Test
    void detectAndFixIsolatedNodes_FoundIsolated() throws Exception {
        // 准备测试数据
        KnowledgePoint p1 = createPoint("p1", "Point 1");
        KnowledgePoint p2 = createPoint("p2", "Point 2");
        List<KnowledgePoint> points = Arrays.asList(p1, p2);

        // 模拟课程存在
        when(courseMapper.findCourseWithTeacher(COURSE_ID)).thenReturn(testCourse);
        // 模拟获取知识点列表
        when(knowledgePointMapper.selectKnowledgePointsByCourseId(COURSE_ID)).thenReturn(points);
        // 没有关系
        when(knowledgeRelationMapper.selectRelationsByCourseId(COURSE_ID)).thenReturn(Collections.emptyList());

        // 创建模拟的AI响应
        String aiResponse = createMockAIResponse();
        when(ollamaService.generateKnowledgeRelations(anyList())).thenReturn(aiResponse);

        // 执行知识图谱获取，会触发孤立节点检测
        KnowledgeGraphDTO result = knowledgePointService.getKnowledgeGraphByCourse(COURSE_ID);

        // 验证AI生成被调用
        verify(ollamaService).generateKnowledgeRelations(anyList());

        // 验证图谱结果
        assertNotNull(result.getNodes());
        assertEquals(2, result.getNodes().size());
        assertNotNull(result.getEdges());
    }

    // ========== 辅助方法 ==========
    private KnowledgePoint createPoint(String id, String name) {
        KnowledgePoint point = new KnowledgePoint();
        point.setPointId(id);
        point.setCourseId(COURSE_ID);
        point.setName(name);
        return point;
    }

    private String createMockAIResponse() {
        // 创建模拟的AI响应JSON
        return "{\"edges\":[{\"source\":\"p1\",\"target\":\"p2\",\"relationType\":\"PREREQUISITE\"}]}";
    }
}
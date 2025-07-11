package com.sx.backend.controller;

import com.sx.backend.controller.KnowledgePointController;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.RelationType;
import com.sx.backend.service.KnowledgePointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KnowledgePointControllerTest {

    @Mock
    private KnowledgePointService knowledgePointService;

    @InjectMocks
    private KnowledgePointController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateKnowledgePoint() {
        KnowledgePoint kp = new KnowledgePoint();
        when(knowledgePointService.createKnowledgePoint("1", kp)).thenReturn(kp);

        ResponseEntity<KnowledgePointController.ResponseResult<KnowledgePoint>> response =
                controller.createKnowledgePoint("1", kp);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("知识点创建成功", response.getBody().getMessage());
        assertEquals(kp, response.getBody().getData());
    }

    @Test
    void testGetKnowledgePointsByCourse() {
        List<KnowledgePoint> list = Arrays.asList(new KnowledgePoint());
        when(knowledgePointService.getKnowledgePointsByCourse("1", false)).thenReturn(list);

        ResponseEntity<KnowledgePointController.ResponseResult<List<KnowledgePoint>>> response =
                controller.getKnowledgePointsByCourse("1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody().getData());
    }

    @Test
    void testGetKnowledgePointById() {
        KnowledgePoint kp = new KnowledgePoint();
        when(knowledgePointService.getKnowledgePointById("2")).thenReturn(kp);

        ResponseEntity<KnowledgePointController.ResponseResult<KnowledgePoint>> response =
                controller.getKnowledgePointById("2");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(kp, response.getBody().getData());
    }

    @Test
    void testUpdateKnowledgePoint() {
        KnowledgePoint kp = new KnowledgePoint();
        when(knowledgePointService.updateKnowledgePoint("3", kp)).thenReturn(kp);

        ResponseEntity<KnowledgePointController.ResponseResult<KnowledgePoint>> response =
                controller.updateKnowledgePoint("3", kp);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(kp, response.getBody().getData());
    }

    @Test
    void testDeleteKnowledgePoint() {
        doNothing().when(knowledgePointService).deleteKnowledgePoint("4");

        ResponseEntity<Void> response = controller.deleteKnowledgePoint("4");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testAddKnowledgeRelation() {
        Map<String, String> req = new HashMap<>();
        req.put("sourcePointId", "a");
        req.put("targetPointId", "b");
        req.put("relationType", "PREREQUISITE");

        doNothing().when(knowledgePointService).addKnowledgeRelation("a", "b", RelationType.PREREQUISITE);

        ResponseEntity<KnowledgePointController.ResponseResult<Map<String, String>>> response =
                controller.addKnowledgeRelation(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("知识点关系添加成功", response.getBody().getMessage());
        assertEquals("a", response.getBody().getData().get("sourcePointId"));
    }

    @Test
    void testRemoveKnowledgeRelation() {
        doNothing().when(knowledgePointService).removeKnowledgeRelation("rel1");

        ResponseEntity<Void> response = controller.removeKnowledgeRelation("rel1");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void testGetKnowledgePointResources() {
        List<Object> resources = Arrays.asList(new Object());
        when(knowledgePointService.getKnowledgePointResources("5")).thenReturn(resources);

        ResponseEntity<KnowledgePointController.ResponseResult<List<Object>>> response =
                controller.getKnowledgePointResources("5");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resources, response.getBody().getData());
    }

    @Test
    void testCheckCircularDependency() {
        when(knowledgePointService.checkCircularDependency("a", "b")).thenReturn(true);

        ResponseEntity<KnowledgePointController.ResponseResult<Boolean>> response =
                controller.checkCircularDependency("a", "b");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData());
    }

    @Test
    void testGetKnowledgeGraph() {
        KnowledgeGraphDTO dto = new KnowledgeGraphDTO();
        when(knowledgePointService.getKnowledgeGraphByCourse("6")).thenReturn(dto);

        ResponseEntity<KnowledgePointController.ResponseResult<KnowledgeGraphDTO>> response =
                controller.getKnowledgeGraph("6");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody().getData());
    }

    @Test
    void testGenerateKnowledgeRelationsByAI_processing() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "PROCESSING");
        when(knowledgePointService.getAIGenerationStatus("7")).thenReturn(status);

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.generateKnowledgeRelationsByAI("7");

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("AI生成任务正在进行中"));
    }

    @Test
    void testGenerateKnowledgeRelationsByAI_exception() {
        when(knowledgePointService.getAIGenerationStatus("9")).thenThrow(new RuntimeException("fail"));

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.generateKnowledgeRelationsByAI("9");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("启动生成任务失败"));
    }

    @Test
    void testGetAIGenerationStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "IDLE");
        when(knowledgePointService.getAIGenerationStatus("10")).thenReturn(status);

        ResponseEntity<KnowledgePointController.ResponseResult<Map<String, Object>>> response =
                controller.getAIGenerationStatus("10");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(status, response.getBody().getData());
    }

    @Test
    void testClearAIGenerationStatus() {
        doNothing().when(knowledgePointService).clearAIGenerationStatus("11");

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.clearAIGenerationStatus("11");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("清理完成", response.getBody().getData());
    }

    @Test
    void testUpdateKnowledgeRelationsIfChanged_success() {
        doNothing().when(knowledgePointService).updateKnowledgeRelationsIfChanged("12");

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.updateKnowledgeRelationsIfChanged("12");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("关系已检查并更新", response.getBody().getData());
    }

    @Test
    void testUpdateKnowledgeRelationsIfChanged_exception() {
        doThrow(new RuntimeException("fail")).when(knowledgePointService).updateKnowledgeRelationsIfChanged("13");

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.updateKnowledgeRelationsIfChanged("13");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("更新关系失败"));
    }

    @Test
    void testForceRegenerateKnowledgeRelations_success() {
        doNothing().when(knowledgePointService).generateKnowledgeRelationsByAI("14");

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.forceRegenerateKnowledgeRelations("14");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("重新生成完成", response.getBody().getData());
    }

    @Test
    void testForceRegenerateKnowledgeRelations_exception() {
        doThrow(new RuntimeException("fail")).when(knowledgePointService).generateKnowledgeRelationsByAI("15");

        ResponseEntity<KnowledgePointController.ResponseResult<String>> response =
                controller.forceRegenerateKnowledgeRelations("15");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("重新生成关系失败"));
    }
}

package com.sx.backend.controller;

import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.RelationType;
import com.sx.backend.service.KnowledgePointService;
import com.sx.backend.dto.KnowledgeGraphDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/teacher")
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    @Autowired
    public KnowledgePointController(KnowledgePointService knowledgePointService) {
        this.knowledgePointService = knowledgePointService;
    }

    // 创建知识点
    @PostMapping("/courses/{courseId}/knowledge-points")
    public ResponseEntity<ResponseResult<KnowledgePoint>> createKnowledgePoint(
            @PathVariable String courseId,
            @RequestBody KnowledgePoint knowledgePoint) {

        KnowledgePoint createdPoint = knowledgePointService.createKnowledgePoint(courseId, knowledgePoint);
        ResponseResult<KnowledgePoint> response = new ResponseResult<>(
                HttpStatus.CREATED.value(), "知识点创建成功", createdPoint);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 获取课程知识点列表
    @GetMapping("/courses/{courseId}/knowledge-points")
    public ResponseEntity<ResponseResult<List<KnowledgePoint>>> getKnowledgePointsByCourse(
            @PathVariable String courseId,
            @RequestParam(required = false, defaultValue = "false") Boolean tree) {

        List<KnowledgePoint> points = knowledgePointService.getKnowledgePointsByCourse(courseId, tree);
        ResponseResult<List<KnowledgePoint>> response = new ResponseResult<>(
                HttpStatus.OK.value(), "成功获取知识点列表", points);
        return ResponseEntity.ok(response);
    }

    // 获取知识点详情
    @GetMapping("/knowledge-points/{pointId}")
    public ResponseEntity<ResponseResult<KnowledgePoint>> getKnowledgePointById(
            @PathVariable String pointId) {

        KnowledgePoint point = knowledgePointService.getKnowledgePointById(pointId);
        ResponseResult<KnowledgePoint> response = new ResponseResult<>(
                HttpStatus.OK.value(), "成功获取知识点详情", point);
        return ResponseEntity.ok(response);
    }

    // 更新知识点信息
    @PutMapping("/knowledge-points/{pointId}")
    public ResponseEntity<ResponseResult<KnowledgePoint>> updateKnowledgePoint(
            @PathVariable String pointId,
            @RequestBody KnowledgePoint knowledgePoint) {

        KnowledgePoint updatedPoint = knowledgePointService.updateKnowledgePoint(pointId, knowledgePoint);
        ResponseResult<KnowledgePoint> response = new ResponseResult<>(
                HttpStatus.OK.value(), "知识点更新成功", updatedPoint);
        return ResponseEntity.ok(response);
    }

    // 删除知识点
    @DeleteMapping("/knowledge-points/{pointId}")
    public ResponseEntity<Void> deleteKnowledgePoint(
            @PathVariable String pointId) {

        knowledgePointService.deleteKnowledgePoint(pointId);
        return ResponseEntity.noContent().build();
    }

    // 添加知识点关系
    @PostMapping("/knowledge-points/relations")
    public ResponseEntity<ResponseResult<Map<String, String>>> addKnowledgeRelation(
            @RequestBody Map<String, String> relationRequest) {

        String sourceId = relationRequest.get("sourcePointId");
        String targetId = relationRequest.get("targetPointId");
        RelationType relationType = RelationType.valueOf(relationRequest.get("relationType"));

        knowledgePointService.addKnowledgeRelation(sourceId, targetId, relationType);

        // 返回关系ID（实际应由服务返回）
        Map<String, String> result = new HashMap<>();
        result.put("sourcePointId", sourceId);
        result.put("targetPointId", targetId);
        result.put("relationType", relationType.name());
        result.put("message", "知识点关系添加成功");

        ResponseResult<Map<String, String>> response = new ResponseResult<>(
                HttpStatus.OK.value(), "知识点关系添加成功", result);
        return ResponseEntity.ok(response);
    }

    // 删除知识点关系
    @DeleteMapping("/knowledge-points/relations/{relationId}")
    public ResponseEntity<Void> removeKnowledgeRelation(
            @PathVariable String relationId) {

        knowledgePointService.removeKnowledgeRelation(relationId);
        return ResponseEntity.noContent().build();
    }

    // 获取知识点关联的资源
    @GetMapping("/knowledge-points/{pointId}/resources")
    public ResponseEntity<ResponseResult<List<Object>>> getKnowledgePointResources(
            @PathVariable String pointId) {

        List<Object> resources = knowledgePointService.getKnowledgePointResources(pointId);
        ResponseResult<List<Object>> response = new ResponseResult<>(
                HttpStatus.OK.value(), "成功获取知识点关联资源", resources);
        return ResponseEntity.ok(response);
    }

    // 更新知识点父节点
    @PatchMapping("/knowledge-points/{pointId}/parent")
    public ResponseEntity<ResponseResult<KnowledgePoint>> updateKnowledgePointParent(
            @PathVariable String pointId,
            @RequestBody Map<String, String> request) {

        String parentId = request.get("parentId");
        // 处理空值情况
        if (parentId != null && parentId.isEmpty()) {
            parentId = null;
        }

        KnowledgePoint updatedPoint = knowledgePointService.updateKnowledgePointParent(pointId, parentId);
        ResponseResult<KnowledgePoint> response = new ResponseResult<>(
                HttpStatus.OK.value(), "知识点父节点更新成功", updatedPoint);
        return ResponseEntity.ok(response);
    }

    // 检查循环依赖
    @GetMapping("/knowledge-points/check-circular")
    public ResponseEntity<ResponseResult<Boolean>> checkCircularDependency(
            @RequestParam String sourceId,
            @RequestParam String targetId) {

        boolean hasCircular = knowledgePointService.checkCircularDependency(sourceId, targetId);
        ResponseResult<Boolean> response = new ResponseResult<>(
                HttpStatus.OK.value(), "循环依赖检查完成", hasCircular);
        return ResponseEntity.ok(response);
    }

    // 获取知识图谱数据
    @GetMapping("/courses/{courseId}/knowledge-graph")
    public ResponseEntity<ResponseResult<KnowledgeGraphDTO>> getKnowledgeGraph(
            @PathVariable String courseId) {

        KnowledgeGraphDTO graph = knowledgePointService.getKnowledgeGraphByCourse(courseId);
        ResponseResult<KnowledgeGraphDTO> response = new ResponseResult<>(
                HttpStatus.OK.value(), "成功获取知识图谱", graph);
        return ResponseEntity.ok(response);
    }

    // AI生成知识点关系（异步处理）
    @PostMapping("/courses/{courseId}/knowledge-points/generate-relations")
    public ResponseEntity<ResponseResult<String>> generateKnowledgeRelationsByAI(
            @PathVariable String courseId) {

        try {
            // 检查是否已经在处理中
            Map<String, Object> status = knowledgePointService.getAIGenerationStatus(courseId);
            if ("PROCESSING".equals(status.get("status"))) {
                ResponseResult<String> response = new ResponseResult<>(
                        HttpStatus.ACCEPTED.value(), "AI生成任务正在进行中，请稍后查询状态", 
                        "任务ID: " + courseId);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            }

            // 启动异步任务
            knowledgePointService.generateKnowledgeRelationsByAIAsync(courseId);
            
            ResponseResult<String> response = new ResponseResult<>(
                    HttpStatus.ACCEPTED.value(), "AI生成任务已启动，请通过状态接口查询进度", 
                    "任务ID: " + courseId);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (Exception e) {
            ResponseResult<String> response = new ResponseResult<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "启动生成任务失败: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 查询AI生成任务状态
    @GetMapping("/courses/{courseId}/knowledge-points/generation-status")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getAIGenerationStatus(
            @PathVariable String courseId) {
        
        Map<String, Object> status = knowledgePointService.getAIGenerationStatus(courseId);
        ResponseResult<Map<String, Object>> response = new ResponseResult<>(
                HttpStatus.OK.value(), "获取任务状态成功", status);
        return ResponseEntity.ok(response);
    }

    // 清理AI生成任务状态
    @DeleteMapping("/courses/{courseId}/knowledge-points/generation-status")
    public ResponseEntity<ResponseResult<String>> clearAIGenerationStatus(
            @PathVariable String courseId) {
        
        knowledgePointService.clearAIGenerationStatus(courseId);
        ResponseResult<String> response = new ResponseResult<>(
                HttpStatus.OK.value(), "任务状态已清理", "清理完成");
        return ResponseEntity.ok(response);
    }

    // 检查并更新知识点关系（在知识点发生变化时调用）
    @PutMapping("/courses/{courseId}/knowledge-points/update-relations")
    public ResponseEntity<ResponseResult<String>> updateKnowledgeRelationsIfChanged(
            @PathVariable String courseId) {
        
        try {
            // 调用检查并更新关系的方法
            knowledgePointService.updateKnowledgeRelationsIfChanged(courseId);
            
            ResponseResult<String> response = new ResponseResult<>(
                    HttpStatus.OK.value(), "知识点关系检查并更新完成", "关系已检查并更新");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseResult<String> response = new ResponseResult<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(), "更新关系失败: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 通用的响应结果类
    public static class ResponseResult<T> {
        private int code;
        private String message;
        private T data;

        public ResponseResult(int code, String message, T data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        // Getters
        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public T getData() {
            return data;
        }

        // Setters
        public void setCode(int code) {
            this.code = code;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
package com.sx.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sx.backend.dto.KnowledgeGraphDTO;
import com.sx.backend.service.KnowledgeGraphService;

@RestController
@RequestMapping("/api/knowledge-graph")
public class KnowledgeGraphController {
    
    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    /**
     * 基于已有知识点生成知识图谱关系
     * @param courseId 课程ID
     * @return 生成的知识图谱
     */
    @PostMapping("/generate-relations/{courseId}")
    public ResponseEntity<KnowledgeGraphDTO> generateRelationsForCourse(@PathVariable String courseId) {
        try {
            KnowledgeGraphDTO result = knowledgeGraphService.generateGraphForCourse(courseId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    /**
     * 获取课程的知识图谱数据
     * @param courseId 课程ID
     * @return 知识图谱数据
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<KnowledgeGraphDTO> getKnowledgeGraphByCourse(@PathVariable String courseId) {
        try {
            KnowledgeGraphDTO result = knowledgeGraphService.getKnowledgeGraphFromDatabase(courseId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 原有方法：基于文件内容生成知识图谱（保持兼容性）
     */
    @PostMapping("/generate")
    public ResponseEntity<KnowledgeGraphDTO> generate(@RequestParam("file") MultipartFile file) {
        try {
            String content = new String(file.getBytes(), "UTF-8");
            KnowledgeGraphDTO result = knowledgeGraphService.generateGraph(content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * 原有方法：基于文本内容生成知识图谱（保持兼容性）
     */
    @PostMapping("/generateByText")
    public ResponseEntity<KnowledgeGraphDTO> generateByText(@RequestBody String content) {
        try {
            KnowledgeGraphDTO result = knowledgeGraphService.generateGraph(content);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}

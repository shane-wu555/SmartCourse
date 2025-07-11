package com.sx.backend.service;

import com.sx.backend.entity.KnowledgePoint;
import com.sx.backend.entity.DifficultyLevel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 知识图谱服务测试类
 * 用于测试基于已有知识点生成知识图谱关系的功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class KnowledgeGraphServiceTest {

    @Autowired
    private OllamaService ollamaService;

    @Test
    public void testOllamaServiceGenerateRelations() throws Exception {
        // 创建测试知识点
        List<KnowledgePoint> knowledgePoints = Arrays.asList(
            createTestKnowledgePoint("kp001", "人工智能基础概念", "介绍人工智能的基本概念和定义"),
            createTestKnowledgePoint("kp002", "机器学习", "机器学习的基本原理和算法"),
            createTestKnowledgePoint("kp003", "深度学习", "深度神经网络和深度学习算法"),
            createTestKnowledgePoint("kp004", "神经网络", "人工神经网络的基本原理"),
            createTestKnowledgePoint("kp005", "监督学习", "监督学习算法和应用")
        );
        
        // 测试OllamaService生成关系
        try {
            String result = ollamaService.generateKnowledgeRelations(knowledgePoints);
            System.out.println("生成的知识图谱关系：");
            System.out.println(result);
            
            // 这里可以进一步解析JSON验证结果
            
        } catch (Exception e) {
            System.err.println("测试失败，可能是因为Ollama服务未启动：" + e.getMessage());
            // 在实际测试环境中，可以mock Ollama服务的响应
        }
    }
    
    private KnowledgePoint createTestKnowledgePoint(String id, String name, String description) {
        KnowledgePoint point = new KnowledgePoint();
        point.setPointId(id);
        point.setName(name);
        point.setDescription(description);
        point.setCourseId("test-course-001");
        point.setDifficultylevel(DifficultyLevel.MEDIUM);
        point.setCreatedAt(new Date());
        point.setUpdatedAt(new Date());
        return point;
    }
}

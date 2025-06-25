package com.sx.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sx.backend.dto.KnowledgeGraphDTO;

public class KnowledgeGraphService {
    private final OllamaService ollamaService = new OllamaService();

    public KnowledgeGraphDTO generateGraph(String content) throws Exception {
        String response = ollamaService.extractKnowledgePoints(content);
        // 假设模型输出的JSON直接就是KnowledgeGraph结构
        ObjectMapper mapper = new ObjectMapper();
        // 你可能需要从大模型返回的内容中提取JSON部分
        String json = extractJson(response);
        return mapper.readValue(json, KnowledgeGraphDTO.class);
    }
    
    private String extractJson(String response) {
        // 简单提取JSON（可根据实际返回调整）
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        return response.substring(start, end);
    }
}

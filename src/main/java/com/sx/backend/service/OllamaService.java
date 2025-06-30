package com.sx.backend.service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import com.sx.backend.entity.KnowledgePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OllamaService {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 基于已有知识点生成知识点之间的关系
     * @param knowledgePoints 已有的知识点列表
     * @return JSON格式的知识图谱数据
     */
    public String generateKnowledgeRelations(List<KnowledgePoint> knowledgePoints) throws Exception {
        // 构建知识点信息字符串
        StringBuilder knowledgeInfo = new StringBuilder();
        knowledgeInfo.append("现有知识点列表：\n");
        for (KnowledgePoint point : knowledgePoints) {
            knowledgeInfo.append("ID: ").append(point.getPointId())
                         .append(", 名称: ").append(point.getName());
            if (point.getDescription() != null && !point.getDescription().isEmpty()) {
                knowledgeInfo.append(", 描述: ").append(point.getDescription());
            }
            knowledgeInfo.append("\n");
        }
        
        System.out.println("发送给AI的知识点信息: " + knowledgeInfo.toString());
        
        String prompt = "请分析以下知识点，并明确节点之间的三种关系：先修（PREREQUISITE）、包含（PART_OF）、相关（RELATED）。" +
                       "严格按照以下JSON格式输出，不要包含任何其他文字：\n" +
                       "{\"nodes\":[{\"id\":\"现有知识点ID\",\"name\":\"知识点名称\",\"description\":\"\",\"difficultylevel\":\"\",\"courseId\":\"\"}],\"edges\":[{\"source\":\"源知识点ID\",\"target\":\"目标知识点ID\",\"relationType\":\"PREREQUISITE\",\"type\":\"先修\"}]}\n" +
                       "要求：\n" +
                       "1. relationType 字段只能为 PREREQUISITE、PART_OF 或 RELATED（大写）\n" +
                       "2. type 字段只能为先修、包含或相关\n" +
                       "3. 节点 id 必须使用现有知识点的真实ID\n" +
                       "4. 边的 source/target 必须是现有知识点的ID\n" +
                       "5. 至少生成3-5条边关系\n" +
                       "6. 只输出JSON，不要解释\n\n" +
                       knowledgeInfo.toString();
                       
        return callOllamaAPI(prompt);
    }
    
    /**
     * 原有方法：基于课程内容提取知识点和关系
     * @param content 课程内容
     * @return JSON格式的知识图谱数据
     */
    public String extractKnowledgePoints(String content) throws Exception {
        String prompt = "请将课程内容细分为多个知识点节点，并明确节点之间的三种关系：先修（PREREQUISITE）、包含（PART_OF）、相关（RELATED）。只输出JSON，格式为{\"nodes\":[{\"id\":\"1\",\"name\":\"xxx\"},...],\"edges\":[{\"source\":\"1\",\"target\":\"2\",\"relationType\":\"PREREQUISITE\",\"type\":\"先修\"},...]}，其中 relationType 字段只能为 PREREQUISITE、PART_OF 或 RELATED（大写），type 字段只能为先修、包含或相关，节点 id 必须唯一，边的 source/target 必须是知识点 id，不要输出任何解释、思考或其它内容。内容如下：" + content;
        return callOllamaAPI(prompt);
    }
    
    /**
     * 调用Ollama API的通用方法 - 修复版本
     */
    private String callOllamaAPI(String prompt) throws Exception {
        // 对 prompt 做 JSON 转义
        String escapedPrompt = prompt.replace("\"", "\\\"")
                                   .replace("\n", "\\n")
                                   .replace("\r", "")
                                   .replace("\t", "\\t");
        
        String requestJson = "{\"model\": \"deepseek-r1\", \"stream\": false, \"messages\": [{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}]}";
        
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost("http://localhost:11434/api/chat");
            post.setEntity(new StringEntity(requestJson, "UTF-8"));
            post.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            System.out.println("Ollama原始响应: " + responseStr);
            
            // 解析Ollama API的响应格式
            try {
                JsonNode responseNode = objectMapper.readTree(responseStr);
                if (responseNode.has("message") && responseNode.get("message").has("content")) {
                    String content = responseNode.get("message").get("content").asText();
                    System.out.println("提取的AI内容: " + content);
                    
                    // 从AI内容中提取JSON
                    String extractedJson = extractJsonFromContent(content);
                    System.out.println("最终提取的JSON: " + extractedJson);
                    
                    return extractedJson;
                } else {
                    System.err.println("Ollama响应格式异常，缺少message.content字段");
                    return "{}";
                }
            } catch (Exception e) {
                System.err.println("解析Ollama响应JSON失败: " + e.getMessage());
                e.printStackTrace();
                return "{}";
            }
        }
    }
    
    /**
     * 从AI返回的内容中提取JSON - 改进版本
     */
    private String extractJsonFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            System.err.println("AI返回内容为空");
            return "{}";
        }
        
        System.out.println("开始提取JSON，原始内容长度: " + content.length());
        
        // 方法1: 优先提取 ```json ... ``` 代码块中的内容
        int codeStart = content.indexOf("```json");
        if (codeStart != -1) {
            System.out.println("找到 ```json 标记");
            int jsonStart = content.indexOf("{", codeStart);
            int codeEnd = content.indexOf("```", codeStart + 7); // 查找结束的```
            if (jsonStart != -1 && codeEnd != -1) {
                int jsonEnd = content.lastIndexOf("}", codeEnd);
                if (jsonEnd > jsonStart) {
                    String result = content.substring(jsonStart, jsonEnd + 1);
                    System.out.println("从 ```json 块中提取JSON成功，长度: " + result.length());
                    return result;
                }
            }
        }
        
        // 方法2: 查找 ``` 代码块（没有明确的json标记）
        int blockStart = content.indexOf("```");
        if (blockStart != -1) {
            System.out.println("找到 ``` 代码块");
            int blockEnd = content.indexOf("```", blockStart + 3);
            if (blockEnd != -1) {
                String blockContent = content.substring(blockStart + 3, blockEnd);
                int jsonStart = blockContent.indexOf("{");
                int jsonEnd = blockContent.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    String result = blockContent.substring(jsonStart, jsonEnd + 1);
                    System.out.println("从代码块中提取JSON成功，长度: " + result.length());
                    return result;
                }
            }
        }
        
        // 方法3: 提取第一个 { 到最后一个 } 之间的内容
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            String result = content.substring(start, end + 1);
            System.out.println("直接提取JSON成功，长度: " + result.length());
            return result;
        }
        
        // 方法4: 尝试查找多个JSON对象中的第一个完整对象
        if (start != -1) {
            int braceCount = 0;
            for (int i = start; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        String result = content.substring(start, i + 1);
                        System.out.println("通过括号匹配提取JSON成功，长度: " + result.length());
                        return result;
                    }
                }
            }
        }
        
        System.err.println("无法从内容中提取有效JSON，返回空对象");
        System.err.println("原始内容前200字符: " + content.substring(0, Math.min(200, content.length())));
        return "{}";
    }
    
    public static void main(String[] args) throws Exception {
        OllamaService service = new OllamaService();
        String testContent = "第一章 绪论：介绍人工智能的基本概念和发展历史。";
        String result = service.extractKnowledgePoints(testContent);
        System.out.println("测试结果: " + result);
    }
}
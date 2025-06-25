package com.sx.backend.service;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

public class OllamaService {
    public String extractKnowledgePoints(String content) throws Exception {
        String prompt = "请将课程内容细分为多个知识点节点，并明确节点之间的三种关系：先修（prerequisite）、包含（part-of）、相关（related）。只输出JSON，格式为{\"nodes\":[{\"id\":\"1\",\"name\":\"xxx\"},...],\"edges\":[{\"source\":\"1\",\"target\":\"2\",\"relationType\":\"prerequisite/part-of/related\",\"type\":\"先修/包含/相关\"},...]}，其中 relationType 字段只能为 prerequisite、part-of 或 related，type 字段只能为“先修”“包含”“相关”，节点 id 必须唯一，边的 source/target 必须是知识点 id，不要输出任何解释、思考或其它内容。内容如下：" + content;
        // 对 prompt 做 JSON 转义
        prompt = prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
        String json = "{\"model\": \"deepseek-r1\", \"stream\": false, \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost("http://localhost:11434/api/chat");
            post.setEntity(new StringEntity(json, "UTF-8"));
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = client.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            // 优先提取 ```json ... ``` 代码块中的内容
            int codeStart = responseStr.indexOf("```json");
            if (codeStart != -1) {
                int jsonStart = responseStr.indexOf("{", codeStart);
                int jsonEnd = responseStr.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    return responseStr.substring(jsonStart, jsonEnd + 1);
                }
            }
            // 否则，提取第一个 { 到最后一个 } 之间的内容
            int start = responseStr.indexOf("{");
            int end = responseStr.lastIndexOf("}");
            if (start != -1 && end != -1 && end > start) {
                return responseStr.substring(start, end + 1);
            }
            return responseStr;
        }
    } 
    
    public static void main(String[] args) throws Exception {
        OllamaService service = new OllamaService();
        String testContent = "第一章 绪论：介绍人工智能的基本概念和发展历史。";
        String result = service.extractKnowledgePoints(testContent);
        System.out.println(result);
    }
}

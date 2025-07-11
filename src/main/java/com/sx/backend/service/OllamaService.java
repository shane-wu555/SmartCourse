package com.sx.backend.service;

import org.apache.http.client.config.RequestConfig;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OllamaService {
    
    private static final Logger logger = LoggerFactory.getLogger(OllamaService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 基于已有知识点生成知识点之间的关系
     * @param knowledgePoints 已有的知识点列表
     * @return JSON格式的知识图谱数据
     */
    public String generateKnowledgeRelations(List<KnowledgePoint> knowledgePoints) throws Exception {
        // 构建知识点信息字符串
        StringBuilder knowledgeInfo = new StringBuilder();
        knowledgeInfo.append("知识点列表：\n");
        for (KnowledgePoint point : knowledgePoints) {
            knowledgeInfo.append("ID: ").append(point.getPointId())
                         .append(", 名称: ").append(point.getName());
            if (point.getDescription() != null && !point.getDescription().isEmpty()) {
                knowledgeInfo.append(", 描述: ").append(point.getDescription());
            }
            knowledgeInfo.append("\n");
        }

        String prompt = "基于以下知识点生成JSON关系图，严格按照格式要求：\n" +
                       "输出格式（必须完全遵循）：\n" +
                       "{\"nodes\":[{\"id\":\"使用提供的UUID\",\"name\":\"知识点名称\"}],\"edges\":[{\"source\":\"源知识点的UUID\",\"target\":\"目标知识点的UUID\",\"relationType\":\"PREREQUISITE\",\"type\":\"先修\"}]}\n" +
                       "关系类型必须是以下之一：\n" +
                       "- PREREQUISITE(先修) - type字段写\"先修\"\n" +
                       "- PART_OF(包含) - type字段写\"包含\"\n" +
                       "- RELATED(相关) - type字段写\"相关\"\n" +
                       "重要要求：\n" +
                       "1. nodes数组中的id字段必须使用下面列表中提供的完整UUID\n" +
                       "2. edges数组中的source和target必须使用UUID，不能使用知识点名称\n" +
                       "3. 必须生成至少" + (knowledgePoints.size()-1) + "条边\n" +
                       "4. 不要添加任何额外字段如type、labels、properties等\n" +
                       "5. 只输出JSON，不要任何解释\n\n" +
                       knowledgeInfo.toString();
        try {
            String aiResult = callOllamaAPI(prompt);
            // 验证和修复AI返回的JSON格式
            return validateAndFixJsonFormat(aiResult, knowledgePoints);
        } catch (Exception e) {
            return generateDefaultKnowledgeRelationsSimple(knowledgePoints);
        }
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
        long startTime = System.currentTimeMillis();
        logger.info("开始调用Ollama API，prompt长度: {}", prompt.length());
        
        // 限制prompt长度
        if (prompt.length() > 8000) {
            prompt = prompt.substring(0, 8000) + "...[内容截断]";
            logger.warn("Prompt过长，已截断至8000字符");
        }

        // 使用 Jackson 的 ObjectMapper 进行安全的 JSON 转义
        String escapedPrompt = objectMapper.writeValueAsString(prompt);
        // 去掉开头和结尾的引号，因为我们要手动构建 JSON
        escapedPrompt = escapedPrompt.substring(1, escapedPrompt.length() - 1);
        
        String requestJson = "{\"model\":\"deepseek-r1\",\"stream\":false,\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]}";

        // 根据任务复杂度动态调整超时时间
        int socketTimeout = 300000; // 5分钟，缩短超时时间
        if (prompt.contains("知识点关系") || prompt.contains("generateKnowledgeRelations")) {
            socketTimeout = 300000; // 5分钟，对于复杂的知识点关系生成
        }
        
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(15000)    // 连接超时15秒，缩短连接时间
            .setSocketTimeout(socketTimeout)    // 动态设置读取超时
            .setConnectionRequestTimeout(10000)  // 连接请求超时10秒
            .build();
            
        logger.info("设置超时时间: 连接{}秒, 读取{}秒", 15, socketTimeout/1000);

        try (CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build()) {
            
            HttpPost post = new HttpPost("http://219.216.65.31:11434/api/chat");
            post.setEntity(new StringEntity(requestJson, StandardCharsets.UTF_8));
            post.setHeader("Content-Type", "application/json");
            
            logger.info("发送请求到Ollama API...");
            HttpResponse response = client.execute(post);
            try {
                String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                long endTime = System.currentTimeMillis();
                logger.info("Ollama API调用完成，耗时: {}ms", (endTime - startTime));
                
                // 简化的日志输出
                logger.debug("Ollama响应: {}", responseStr.length() > 200 ? 
                    responseStr.substring(0, 200) + "..." : responseStr);
                
                JsonNode responseNode = objectMapper.readTree(responseStr);
                if (responseNode.has("message") && responseNode.get("message").has("content")) {
                    String content = responseNode.get("message").get("content").asText();
                    return extractJsonFromContent(content);
                }
                return "{}";
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        } catch (java.net.SocketTimeoutException e) {
            long endTime = System.currentTimeMillis();
            logger.warn("调用Ollama API超时，耗时: {}ms, 超时设置: {}ms, 生成默认知识点关系: {}", 
                (endTime - startTime), socketTimeout, e.getMessage());
            return generateDefaultKnowledgeRelations(prompt);
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            logger.error("调用Ollama API失败，耗时: {}ms", (endTime - startTime), e);
            throw e;
        }
    }
    
    /**
     * 从AI返回的内容中提取JSON - 改进版本
     */
    private String extractJsonFromContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            logger.warn("AI返回内容为空");
            return "{}";
        }
        
        logger.debug("开始提取JSON，原始内容长度: {}", content.length());
        
        // 方法1: 优先提取 ```json ... ``` 代码块中的内容
        int codeStart = content.indexOf("```json");
        if (codeStart != -1) {
            logger.debug("找到 ```json 标记");
            int jsonStart = content.indexOf("{", codeStart);
            int codeEnd = content.indexOf("```", codeStart + 7); // 查找结束的```
            if (jsonStart != -1 && codeEnd != -1) {
                int jsonEnd = content.lastIndexOf("}", codeEnd);
                if (jsonEnd > jsonStart) {
                    String result = content.substring(jsonStart, jsonEnd + 1);
                    logger.debug("从 ```json 块中提取JSON成功，长度: {}", result.length());
                    return result;
                }
            }
        }
        
        // 方法2: 查找 ``` 代码块（没有明确的json标记）
        int blockStart = content.indexOf("```");
        if (blockStart != -1) {
            logger.debug("找到 ``` 代码块");
            int blockEnd = content.indexOf("```", blockStart + 3);
            if (blockEnd != -1) {
                String blockContent = content.substring(blockStart + 3, blockEnd);
                int jsonStart = blockContent.indexOf("{");
                int jsonEnd = blockContent.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                    String result = blockContent.substring(jsonStart, jsonEnd + 1);
                    logger.debug("从代码块中提取JSON成功，长度: {}", result.length());
                    return result;
                }
            }
        }
        
        // 方法3: 提取第一个 { 到最后一个 } 之间的内容
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start != -1 && end != -1 && end > start) {
            String result = content.substring(start, end + 1);
            logger.debug("直接提取JSON成功，长度: {}", result.length());
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
                        logger.debug("通过括号匹配提取JSON成功，长度: {}", result.length());
                        return result;
                    }
                }
            }
        }
        
        logger.warn("无法从内容中提取有效JSON，返回空对象");
        logger.debug("原始内容前200字符: {}", content.substring(0, Math.min(200, content.length())));
        return "{}";
    }
    
    /**
     * 公共方法：调用Ollama API生成推荐建议
     * @param prompt 提示词
     * @return AI生成的回复（纯文本）
     */
    public String generateRecommendationSuggestion(String prompt) throws Exception {
        return callOllamaAPIForText(prompt);
    }
    
    /**
     * 调用Ollama API获取纯文本响应
     */
    private String callOllamaAPIForText(String prompt) throws Exception {
        // 对 prompt 做 JSON 转义
        String escapedPrompt = prompt.replace("\"", "\\\"")
                                   .replace("\n", "\\n")
                                   .replace("\r", "")
                                   .replace("\t", "\\t");
        
        String requestJson = "{\"model\": \"deepseek-r1\", \"stream\": false, \"messages\": [{\"role\": \"user\", \"content\": \"" + escapedPrompt + "\"}]}";
        
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost("http://219.216.65.31:11434/api/chat");
            post.setEntity(new StringEntity(requestJson, "UTF-8"));
            post.setHeader("Content-Type", "application/json");
            
            HttpResponse response = client.execute(post);
            String responseStr = EntityUtils.toString(response.getEntity(), "UTF-8");
            
            logger.debug("Ollama原始响应长度: {}", responseStr.length());
            
            // 解析Ollama API的响应格式
            try {
                JsonNode responseNode = objectMapper.readTree(responseStr);
                if (responseNode.has("message") && responseNode.get("message").has("content")) {
                    String content = responseNode.get("message").get("content").asText();
                    logger.debug("提取的AI内容长度: {}", content.length());
                    
                    // 去除 <think> 标签内容，只返回实际的建议内容
                    String cleanedContent = removeThinkTags(content);
                    logger.debug("清理后的内容长度: {}", cleanedContent.length());
                    
                    return cleanedContent;
                } else {
                    logger.error("Ollama响应格式异常，缺少message.content字段");
                    return "抱歉，无法生成建议。";
                }
            } catch (Exception e) {
                logger.error("解析Ollama响应JSON失败: {}", e.getMessage());
                return "抱歉，无法生成建议。";
            }
        }
    }
    
    /**
     * 移除 <think> 标签内容，只保留实际的建议内容
     */
    private String removeThinkTags(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        // 移除 <think>...</think> 标签及其内容
        String result = content.replaceAll("(?s)<think>.*?</think>", "").trim();
        
        // 如果结果为空，返回原始内容
        if (result.isEmpty()) {
            return content;
        }
        
        return result;
    }
    
    /**
     * 生成默认的知识点关系（当AI调用失败时的备用方案）
     * 使用相关关系作为安全的默认选项
     */
    private String generateDefaultKnowledgeRelations(String originalPrompt) {
        logger.info("生成默认知识点关系作为备用方案");
        
        // 从原始prompt中提取知识点信息 - 修复字符串查找
        String knowledgeInfo;
        int startIndex = originalPrompt.indexOf("知识点列表：");
        if (startIndex == -1) {
            // 如果找不到新格式，尝试旧格式
            startIndex = originalPrompt.indexOf("现有知识点列表：");
            if (startIndex == -1) {
                logger.warn("无法在prompt中找到知识点列表，使用整个prompt");
                knowledgeInfo = originalPrompt;
            } else {
                knowledgeInfo = originalPrompt.substring(startIndex);
            }
        } else {
            knowledgeInfo = originalPrompt.substring(startIndex);
        }
        
        String[] lines = knowledgeInfo.split("\n");
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"nodes\":[");
        
        // 提取知识点信息
        boolean first = true;
        String[] ids = new String[10]; // 增加数组大小
        String[] names = new String[10];
        int idIndex = 0;
        
        for (String line : lines) {
            if (line.startsWith("ID: ") && idIndex < 10) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                first = false;
                
                // 提取ID和名称
                String id = line.substring(4, line.indexOf(", 名称"));
                String name = line.substring(line.indexOf("名称: ") + 3);
                if (name.contains(", 描述")) {
                    name = name.substring(0, name.indexOf(", 描述"));
                }
                
                ids[idIndex] = id;
                names[idIndex] = name;
                idIndex++;
                
                jsonBuilder.append("{\"id\":\"").append(id).append("\",\"name\":\"").append(name).append("\"}");
            }
        }
        
        jsonBuilder.append("],\"edges\":[");
        
        // 生成多种类型的关系而非仅相关关系
        // 策略：让每个知识点与其他知识点建立不同类型的关系
        boolean firstEdge = true;
        int edgeCount = 0;
        int targetEdges = Math.max(1, idIndex - 1); // 至少1条边，目标是n-1条边
        
        for (int i = 0; i < idIndex - 1 && edgeCount < targetEdges; i++) {
            if (!firstEdge) {
                jsonBuilder.append(",");
            }
            firstEdge = false;
            
            // 根据知识点名称和位置选择关系类型
            String relationType = "RELATED";
            String typeText = "相关";
            
            if (names[i] != null && names[i + 1] != null) {
                String sourceName = names[i].toLowerCase();
                String targetName = names[i + 1].toLowerCase();
                
                // 简单的关系判断逻辑
                if (sourceName.contains("基础") || sourceName.contains("入门") || sourceName.contains("概述")) {
                    relationType = "PREREQUISITE";
                    typeText = "先修";
                } else if (sourceName.contains("章") && targetName.contains("节")) {
                    relationType = "PART_OF";
                    typeText = "包含";
                } else if (i == 0) {
                    // 第一个知识点通常是后续的先修条件
                    relationType = "PREREQUISITE";
                    typeText = "先修";
                }
            }
            
            jsonBuilder.append("{\"source\":\"").append(ids[i]).append("\",\"target\":\"").append(ids[i + 1])
                      .append("\",\"relationType\":\"").append(relationType).append("\",\"type\":\"").append(typeText).append("\"}");
            edgeCount++;
        }
        
        // 如果知识点数量大于3，再添加一些跨越式的相关关系
        if (idIndex > 3 && edgeCount < targetEdges) {
            for (int i = 0; i < idIndex - 2 && edgeCount < targetEdges; i += 2) {
                jsonBuilder.append(",");
                jsonBuilder.append("{\"source\":\"").append(ids[i]).append("\",\"target\":\"").append(ids[i + 2])
                          .append("\",\"relationType\":\"RELATED\",\"type\":\"相关\"}");
                edgeCount++;
            }
        }
        
        jsonBuilder.append("]}");
        
        String result = jsonBuilder.toString();
        logger.info("默认关系图生成完成，包含 {} 个节点，{} 条多类型关系", idIndex, edgeCount);
        return result;
    }

    /**
     * 直接基于知识点对象生成简单的默认关系（不依赖prompt解析）
     */
    private String generateDefaultKnowledgeRelationsSimple(List<KnowledgePoint> knowledgePoints) {
        logger.info("生成简单默认关系，知识点数量: {}", knowledgePoints.size());
        
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"nodes\":[");
        
        // 直接使用知识点对象生成nodes
        for (int i = 0; i < knowledgePoints.size(); i++) {
            if (i > 0) {
                jsonBuilder.append(",");
            }
            KnowledgePoint point = knowledgePoints.get(i);
            jsonBuilder.append("{\"id\":\"").append(point.getPointId())
                      .append("\",\"name\":\"").append(point.getName()).append("\"}");
        }
        
        jsonBuilder.append("],\"edges\":[");
        
        // 生成多种类型的关系
        boolean firstEdge = true;
        int targetEdges = Math.max(knowledgePoints.size() - 1, 1); // 至少生成 size-1 条边
        int edgeCount = 0;
        
        // 策略1：基础连接（相关关系）
        for (int i = 0; i < knowledgePoints.size() - 1 && edgeCount < targetEdges; i++) {
            if (!firstEdge) {
                jsonBuilder.append(",");
            }
            firstEdge = false;
            
            KnowledgePoint source = knowledgePoints.get(i);
            KnowledgePoint target = knowledgePoints.get(i + 1);
            
            // 根据知识点名称简单判断关系类型
            String relationType = "RELATED";
            String typeText = "相关";
            
            String sourceName = source.getName().toLowerCase();
            String targetName = target.getName().toLowerCase();
            
            // 简单的关系判断逻辑
            if (sourceName.contains("基础") || sourceName.contains("入门") || sourceName.contains("概述")) {
                relationType = "PREREQUISITE";
                typeText = "先修";
            } else if (sourceName.contains("章") && targetName.contains("节")) {
                relationType = "PART_OF";
                typeText = "包含";
            }
            
            jsonBuilder.append("{\"source\":\"").append(source.getPointId())
                      .append("\",\"target\":\"").append(target.getPointId())
                      .append("\",\"relationType\":\"").append(relationType)
                      .append("\",\"type\":\"").append(typeText).append("\"}");
            edgeCount++;
        }
        
        // 策略2：如果还需要更多边，添加跨越式连接
        if (knowledgePoints.size() > 3 && edgeCount < targetEdges) {
            for (int i = 0; i < knowledgePoints.size() - 2 && edgeCount < targetEdges; i += 2) {
                jsonBuilder.append(",");
                
                KnowledgePoint source = knowledgePoints.get(i);
                KnowledgePoint target = knowledgePoints.get(i + 2);
                
                jsonBuilder.append("{\"source\":\"").append(source.getPointId())
                          .append("\",\"target\":\"").append(target.getPointId())
                          .append("\",\"relationType\":\"RELATED\",\"type\":\"相关\"}");
                edgeCount++;
            }
        }
        
        jsonBuilder.append("]}");
        
        String result = jsonBuilder.toString();
        logger.info("简单默认关系生成完成，包含 {} 个节点，{} 条边", knowledgePoints.size(), edgeCount);
        return result;
    }

    /**
     * 验证和修复AI返回的JSON格式
     */
    private String validateAndFixJsonFormat(String jsonStr, List<KnowledgePoint> knowledgePoints) {
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            
            // 检查基本结构
            if (!jsonNode.has("nodes") || !jsonNode.has("edges")) {
                logger.warn("JSON缺少nodes或edges字段，使用默认关系");
                return generateDefaultKnowledgeRelationsSimple(knowledgePoints);
            }
            
            JsonNode nodesArray = jsonNode.get("nodes");
            JsonNode edgesArray = jsonNode.get("edges");
            
            if (!nodesArray.isArray() || !edgesArray.isArray()) {
                logger.warn("nodes或edges不是数组格式，使用默认关系");
                return generateDefaultKnowledgeRelationsSimple(knowledgePoints);
            }
            
            // 验证nodes格式并修复
            StringBuilder fixedJson = new StringBuilder();
            fixedJson.append("{\"nodes\":[");
            
            boolean firstNode = true;
            Set<String> validIds = new java.util.HashSet<>();
            
            // 收集有效的知识点ID
            for (KnowledgePoint point : knowledgePoints) {
                validIds.add(point.getPointId());
            }
            
            for (KnowledgePoint point : knowledgePoints) {
                if (!firstNode) {
                    fixedJson.append(",");
                }
                firstNode = false;
                fixedJson.append("{\"id\":\"").append(point.getPointId())
                        .append("\",\"name\":\"").append(point.getName()).append("\"}");
            }
            
            fixedJson.append("],\"edges\":[");
            
            // 验证和修复edges格式
            boolean firstEdge = true;
            int validEdgeCount = 0;
            
            for (JsonNode edge : edgesArray) {
                if (edge.has("source") && edge.has("target") && 
                    edge.has("relationType") && edge.has("type")) {
                    
                    String source = edge.get("source").asText();
                    String target = edge.get("target").asText();
                    String relationType = edge.get("relationType").asText();
                    String type = edge.get("type").asText();
                    
                    // 检查ID是否有效
                    if (validIds.contains(source) && validIds.contains(target) && !source.equals(target)) {
                        if (!firstEdge) {
                            fixedJson.append(",");
                        }
                        firstEdge = false;
                        
                        // 验证关系类型
                        if (!relationType.equals("PREREQUISITE") && !relationType.equals("PART_OF") && !relationType.equals("RELATED")) {
                            relationType = "RELATED";
                            type = "相关";
                        }
                        
                        fixedJson.append("{\"source\":\"").append(source)
                                .append("\",\"target\":\"").append(target)
                                .append("\",\"relationType\":\"").append(relationType)
                                .append("\",\"type\":\"").append(type).append("\"}");
                        validEdgeCount++;
                    }
                }
            }
            
            // 如果有效边数不够，补充默认边
            if (validEdgeCount < knowledgePoints.size() - 1) {
                logger.warn("有效边数不足，补充默认边");
                List<String> pointIds = knowledgePoints.stream()
                        .map(KnowledgePoint::getPointId)
                        .collect(Collectors.toList());
                        
                for (int i = validEdgeCount; i < Math.min(knowledgePoints.size() - 1, pointIds.size() - 1); i++) {
                    if (!firstEdge) {
                        fixedJson.append(",");
                    }
                    firstEdge = false;
                    
                    fixedJson.append("{\"source\":\"").append(pointIds.get(i))
                            .append("\",\"target\":\"").append(pointIds.get(i + 1))
                            .append("\",\"relationType\":\"RELATED\",\"type\":\"相关\"}");
                }
            }
            
            fixedJson.append("]}");
            
            String result = fixedJson.toString();
            logger.info("JSON格式验证和修复完成，有效边数: {}", validEdgeCount);
            return result;
            
        } catch (Exception e) {
            logger.error("JSON验证失败，使用默认关系: {}", e.getMessage());
            return generateDefaultKnowledgeRelationsSimple(knowledgePoints);
        }
    }

    /**
     * 测试Ollama API连接状态
     */
    public boolean testOllamaConnection() {
        try {
            RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)    // 连接超时10秒
                .setSocketTimeout(30000)     // 读取超时30秒
                .setConnectionRequestTimeout(5000)   // 连接请求超时5秒
                .build();

            try (CloseableHttpClient client = HttpClientBuilder.create()
                    .setDefaultRequestConfig(config)
                    .build()) {
                
                // 发送简单的测试请求
                String testJson = "{\"model\":\"deepseek-r1\",\"stream\":false,\"messages\":[{\"role\":\"user\",\"content\":\"测试连接\"}]}";
                HttpPost post = new HttpPost("http://219.216.65.31:11434/api/chat");
                post.setEntity(new StringEntity(testJson, StandardCharsets.UTF_8));
                post.setHeader("Content-Type", "application/json");
                
                logger.info("测试Ollama API连接...");
                long startTime = System.currentTimeMillis();
                HttpResponse response = client.execute(post);
                long endTime = System.currentTimeMillis();
                
                try {
                    EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    logger.info("连接测试成功，响应时间: {}ms, 状态码: {}", 
                        (endTime - startTime), response.getStatusLine().getStatusCode());
                    return response.getStatusLine().getStatusCode() == 200;
                } finally {
                    EntityUtils.consume(response.getEntity());
                }
            }
        } catch (Exception e) {
            logger.error("Ollama API连接测试失败", e);
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        Logger mainLogger = LoggerFactory.getLogger(OllamaService.class);
        OllamaService service = new OllamaService();
        
        // 先测试连接
        boolean connected = service.testOllamaConnection();
        mainLogger.info("连接状态: {}", (connected ? "成功" : "失败"));
        
        // 测试知识点提取
        String testContent = "第一章 绪论：介绍人工智能的基本概念和发展历史。";
        String result = service.extractKnowledgePoints(testContent);
        mainLogger.info("测试结果: {}", result);
    }
}
# AI学习推荐系统API文档

## 概述

AI学习推荐系统基于学生的成绩评估数据，利用大模型智能分析学生的学习状态，为学生推荐适合的知识点和学习资源，帮助学生提高学习效果。

## 功能特性

1. **智能成绩分析**：综合分析学生的课程总成绩、各任务成绩、班级排名等数据
2. **知识点推荐**：识别学生的薄弱知识点，按优先级推荐需要重点学习的内容
3. **资源推荐**：基于薄弱知识点推荐相关的学习资源（视频、文档、PPT等）
4. **AI建议生成**：调用大模型生成个性化的学习建议和改进方案
5. **学习路径规划**：为学生提供循序渐进的学习路径建议

## API接口

### 1. 生成学习推荐

**POST** `/api/recommendation/generate`

根据请求类型生成对应的学习推荐。

#### 请求体

```json
{
  "courseId": "course123",
  "type": "knowledge_point",
  "limit": 5,
  "minScoreThreshold": 60.0
}
```

#### 请求参数

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| courseId | String | 是 | 课程ID |
| type | String | 是 | 推荐类型：knowledge_point（知识点）、resource（资源）、comprehensive（综合） |
| limit | Integer | 否 | 推荐数量限制，默认5 |
| minScoreThreshold | Float | 否 | 最低成绩阈值，默认60.0 |

#### 响应示例

```json
{
  "code": 200,
  "message": "推荐生成成功",
  "data": {
    "studentId": "student123",
    "courseId": "course123",
    "type": "knowledge_point",
    "knowledgePointRecommendations": [
      {
        "pointId": "kp001",
        "name": "数据结构基础",
        "description": "数组、链表、栈、队列等基本数据结构",
        "difficultyLevel": "MEDIUM",
        "reason": "该知识点掌握程度较低(45.0%)，建议重点学习",
        "priority": 8,
        "masteryLevel": 45.0,
        "resourceCount": 5,
        "isWeakPoint": true
      }
    ],
    "overallSuggestion": "建议重点关注数据结构基础知识点，通过练习和复习来提高掌握程度。",
    "currentGrade": 75.5,
    "classRank": 15,
    "generatedTime": "2025-01-10 14:30:00"
  }
}
```

### 2. 获取知识点推荐

**GET** `/api/recommendation/knowledge-points`

获取基于成绩分析的知识点推荐。

#### 请求参数

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| courseId | String | 是 | 课程ID |
| limit | Integer | 否 | 推荐数量限制，默认5 |

#### 响应示例

```json
{
  "code": 200,
  "message": "知识点推荐获取成功",
  "data": {
    "studentId": "student123",
    "courseId": "course123",
    "type": "knowledge_point",
    "knowledgePointRecommendations": [
      {
        "pointId": "kp001",
        "name": "算法复杂度分析",
        "description": "时间复杂度和空间复杂度的计算方法",
        "difficultyLevel": "HARD",
        "reason": "该知识点掌握程度较低(35.0%)，建议重点学习",
        "priority": 9,
        "masteryLevel": 35.0,
        "resourceCount": 3,
        "isWeakPoint": true
      },
      {
        "pointId": "kp002",
        "name": "排序算法",
        "description": "冒泡排序、选择排序、快速排序等",
        "difficultyLevel": "MEDIUM",
        "reason": "该知识点掌握程度一般(55.0%)，需要进一步巩固",
        "priority": 7,
        "masteryLevel": 55.0,
        "resourceCount": 8,
        "isWeakPoint": true
      }
    ],
    "overallSuggestion": "根据AI分析，建议您首先掌握算法复杂度分析的基本概念，然后深入学习各种排序算法的实现原理。",
    "currentGrade": 75.5,
    "classRank": 15,
    "generatedTime": "2025-01-10 14:30:00"
  }
}
```

### 3. 获取资源推荐

**GET** `/api/recommendation/resources`

获取基于薄弱知识点的学习资源推荐。

#### 请求参数

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| courseId | String | 是 | 课程ID |
| limit | Integer | 否 | 推荐数量限制，默认5 |

#### 响应示例

```json
{
  "code": 200,
  "message": "资源推荐获取成功",
  "data": {
    "studentId": "student123",
    "courseId": "course123",
    "type": "resource",
    "resourceRecommendations": [
      {
        "resourceId": "res001",
        "name": "数据结构视频教程",
        "type": "VIDEO",
        "url": "/resources/course123/data_structure_tutorial.mp4",
        "description": "详细讲解数据结构的基本概念和实现",
        "reason": "视频资源有助于直观理解「数据结构基础」相关概念",
        "priority": 8,
        "relatedKnowledgePointId": "kp001",
        "relatedKnowledgePointName": "数据结构基础",
        "size": 157286400,
        "duration": 1800.0,
        "viewCount": 45,
        "isHighPriority": true
      },
      {
        "resourceId": "res002",
        "name": "算法复杂度分析PPT",
        "type": "PPT",
        "url": "/resources/course123/complexity_analysis.pptx",
        "description": "算法复杂度分析的理论基础和计算方法",
        "reason": "PPT资源系统梳理「算法复杂度分析」的知识框架",
        "priority": 7,
        "relatedKnowledgePointId": "kp002",
        "relatedKnowledgePointName": "算法复杂度分析",
        "size": 2048000,
        "duration": null,
        "viewCount": 23,
        "isHighPriority": true
      }
    ],
    "overallSuggestion": "建议您优先学习视频教程来理解数据结构的基本概念，然后通过PPT资源系统学习算法复杂度分析。",
    "currentGrade": 75.5,
    "classRank": 15,
    "generatedTime": "2025-01-10 14:30:00"
  }
}
```

### 4. 获取综合推荐

**GET** `/api/recommendation/comprehensive`

获取包含知识点推荐、资源推荐和综合学习建议的完整推荐方案。

#### 请求参数

| 参数 | 类型 | 必需 | 说明 |
|------|------|------|------|
| courseId | String | 是 | 课程ID |

#### 响应示例

```json
{
  "code": 200,
  "message": "综合推荐获取成功",
  "data": {
    "studentId": "student123",
    "courseId": "course123",
    "type": "comprehensive",
    "knowledgePointRecommendations": [
      {
        "pointId": "kp001",
        "name": "数据结构基础",
        "description": "数组、链表、栈、队列等基本数据结构",
        "difficultyLevel": "MEDIUM",
        "reason": "该知识点掌握程度较低(45.0%)，建议重点学习",
        "priority": 8,
        "masteryLevel": 45.0,
        "resourceCount": 5,
        "isWeakPoint": true
      }
    ],
    "resourceRecommendations": [
      {
        "resourceId": "res001",
        "name": "数据结构视频教程",
        "type": "VIDEO",
        "url": "/resources/course123/data_structure_tutorial.mp4",
        "description": "详细讲解数据结构的基本概念和实现",
        "reason": "视频资源有助于直观理解「数据结构基础」相关概念",
        "priority": 8,
        "relatedKnowledgePointId": "kp001",
        "relatedKnowledgePointName": "数据结构基础",
        "size": 157286400,
        "duration": 1800.0,
        "viewCount": 45,
        "isHighPriority": true
      }
    ],
    "overallSuggestion": "基于AI分析，建议您制定系统的学习计划：1）首先通过视频教程理解数据结构基本概念；2）结合编程练习巩固理论知识；3）定期复习并完成相关作业。预计通过2-3周的集中学习可以显著提高这些知识点的掌握程度。",
    "currentGrade": 75.5,
    "classRank": 15,
    "learningStatus": "中等",
    "learningPath": "建议学习路径：数据结构基础 → 算法复杂度分析 → 排序算法",
    "expectedImprovement": 15.0,
    "generatedTime": "2025-01-10 14:30:00"
  }
}
```

## 数据模型

### RecommendationRequest

```json
{
  "studentId": "string",      // 学生ID（自动获取）
  "courseId": "string",       // 课程ID
  "type": "string",           // 推荐类型
  "limit": "integer",         // 推荐数量限制
  "minScoreThreshold": "float" // 最低成绩阈值
}
```

### KnowledgePointRecommendation

```json
{
  "pointId": "string",        // 知识点ID
  "name": "string",           // 知识点名称
  "description": "string",    // 知识点描述
  "difficultyLevel": "string", // 难度级别
  "reason": "string",         // 推荐理由
  "priority": "integer",      // 推荐优先级(1-10)
  "masteryLevel": "float",    // 掌握程度(0-100)
  "resourceCount": "integer", // 关联资源数量
  "isWeakPoint": "boolean"    // 是否为薄弱知识点
}
```

### ResourceRecommendation

```json
{
  "resourceId": "string",            // 资源ID
  "name": "string",                  // 资源名称
  "type": "string",                  // 资源类型
  "url": "string",                   // 资源URL
  "description": "string",           // 资源描述
  "reason": "string",                // 推荐理由
  "priority": "integer",             // 推荐优先级(1-10)
  "relatedKnowledgePointId": "string", // 关联知识点ID
  "relatedKnowledgePointName": "string", // 关联知识点名称
  "size": "long",                    // 资源大小
  "duration": "float",               // 资源时长
  "viewCount": "integer",            // 观看次数
  "isHighPriority": "boolean"        // 是否高优先级
}
```

## 推荐算法

### 1. 知识点推荐算法

1. **数据收集**：获取学生的课程成绩、任务成绩、班级排名等数据
2. **掌握程度计算**：基于任务成绩计算每个知识点的掌握程度
3. **薄弱点识别**：识别掌握程度低于阈值的知识点
4. **优先级计算**：综合考虑掌握程度、难度级别等因素计算推荐优先级
5. **AI建议生成**：调用大模型生成个性化学习建议

### 2. 资源推荐算法

1. **关联分析**：分析资源与知识点的关联关系
2. **薄弱点匹配**：为薄弱知识点匹配相关资源
3. **资源优先级**：基于资源类型、观看次数等计算优先级
4. **个性化排序**：根据学生特点对资源进行排序

### 3. AI建议生成

1. **数据整理**：整理学生成绩、知识点掌握情况、推荐资源等信息
2. **提示词构建**：构建包含学生情况和推荐内容的提示词
3. **大模型调用**：调用Ollama服务生成个性化建议
4. **结果处理**：提取并格式化AI生成的建议内容

## 部署要求

1. **依赖服务**：需要Ollama服务运行在 `http://localhost:11434`
2. **模型要求**：使用 `deepseek-r1` 模型
3. **数据库要求**：需要完整的成绩、知识点、资源数据
4. **权限要求**：需要学生身份验证和授权

## 注意事项

1. **数据隐私**：严格保护学生成绩和学习数据
2. **AI服务依赖**：确保Ollama服务可用性
3. **性能优化**：大量数据处理时可能需要异步处理
4. **准确性**：AI建议仅供参考，不能完全替代人工指导
5. **实时性**：推荐结果基于最新的成绩数据生成

## 扩展功能

1. **学习效果追踪**：跟踪学生按推荐学习后的效果
2. **个性化调整**：根据学生反馈调整推荐算法
3. **学习计划生成**：生成详细的学习时间安排
4. **同伴推荐**：推荐学习伙伴或学习小组
5. **进度提醒**：定期提醒学生学习进度

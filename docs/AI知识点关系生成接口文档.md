# AI知识点关系生成接口文档

本文档描述了基于AI大模型生成知识点关系的功能实现。

## 功能特性

1. **自动生成关系**: 基于课程中已有的知识点，调用AI大模型自动生成知识点之间的关系
2. **智能更新**: 当知识点发生变化时，系统会自动重新生成关系
3. **关系类型**: 支持三种关系类型：
   - `PREREQUISITE`: 先修关系
   - `PART_OF`: 包含关系  
   - `RELATED`: 相关关系

## API接口

### 1. 生成知识点关系

**接口地址**: `POST /api/teacher/courses/{courseId}/knowledge-points/generate-relations`

**功能**: 基于AI为指定课程的知识点生成关系并保存到数据库

**请求参数**:
- `courseId` (路径参数): 课程ID

**响应示例**:
```json
{
    "code": 200,
    "message": "AI生成知识点关系成功",
    "data": "关系生成完成"
}
```

### 2. 更新知识点关系

**接口地址**: `PUT /api/teacher/courses/{courseId}/knowledge-points/update-relations`

**功能**: 检查知识点变化并重新生成关系

**请求参数**:
- `courseId` (路径参数): 课程ID

**响应示例**:
```json
{
    "code": 200,
    "message": "知识点关系更新完成",
    "data": "关系已重新生成"
}
```

### 3. 获取知识图谱

**接口地址**: `GET /api/teacher/courses/{courseId}/knowledge-graph`

**功能**: 获取课程的知识图谱数据（包含节点和边）

**请求参数**:
- `courseId` (路径参数): 课程ID

**响应示例**:
```json
{
    "code": 200,
    "message": "成功获取知识图谱",
    "data": {
        "nodes": [
            {
                "id": "point-1",
                "name": "基础概念",
                "description": "课程基础概念介绍",
                "difficultylevel": "EASY",
                "courseId": "course-1"
            }
        ],
        "edges": [
            {
                "source": "point-1",
                "target": "point-2",
                "relationType": "prerequisite",
                "type": "先修"
            }
        ]
    }
}
```

## 使用流程

### 1. 创建知识点后生成关系

```bash
# 1. 创建知识点
curl -X POST "http://localhost:8080/api/teacher/courses/course-1/knowledge-points" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Java基础",
    "description": "Java编程语言基础知识",
    "difficultylevel": "EASY"
  }'

# 2. 生成AI关系
curl -X POST "http://localhost:8080/api/teacher/courses/course-1/knowledge-points/generate-relations"
```

### 2. 知识点变化后更新关系

```bash
# 1. 修改知识点
curl -X PUT "http://localhost:8080/api/teacher/knowledge-points/point-1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Java高级特性",
    "description": "Java高级编程特性",
    "difficultylevel": "HARD"
  }'

# 2. 更新关系
curl -X PUT "http://localhost:8080/api/teacher/courses/course-1/knowledge-points/update-relations"
```

## 实现原理

### AI关系生成流程

1. **获取知识点**: 从数据库获取课程下的所有知识点
2. **调用AI服务**: 将知识点信息发送给Ollama AI服务
3. **解析响应**: 解析AI返回的JSON关系数据
4. **验证保存**: 验证关系的有效性并保存到数据库

### 关系更新策略

1. **变化检测**: 比较当前知识点与现有关系中的知识点
2. **自动重生成**: 发现变化时自动删除旧关系并重新生成
3. **事务保护**: 整个过程在数据库事务中执行

## 注意事项

1. **AI服务依赖**: 需要确保Ollama服务正在运行（默认地址：http://localhost:11434）
2. **数据一致性**: 生成关系前会删除课程现有的所有关系
3. **性能考虑**: 对于大量知识点的课程，AI生成可能需要较长时间
4. **错误处理**: 如果AI服务不可用或返回格式错误，会抛出相应异常

## 数据库变更

### 新增Mapper方法

在 `KnowledgeRelationMapper` 中新增：
```java
int deleteRelationsByCourseId(String courseId);
```

对应的SQL映射：
```xml
<delete id="deleteRelationsByCourseId">
    DELETE FROM knowledge_relation
    WHERE source_point_id IN (
        SELECT point_id FROM knowledge_point WHERE course_id = #{courseId}
    )
</delete>
```

这样，当知识点发生变化时，系统能够智能地重新生成知识点关系，确保知识图谱的准确性和完整性。

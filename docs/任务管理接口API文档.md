### 任务管理接口API文档

---

# 任务管理API文档（支持知识点绑定）

## 文档说明
- **基础URL**：`暂无`
- **认证方式**：Bearer Token (JWT)
- **权限要求**：
  - 教师角色：创建/更新/删除任务
  - 学生角色：提交任务/查看任务
- **数据格式**：JSON（除文件上传使用multipart/form-data）
- **时间格式**：ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`)

---

## 1. 创建任务（支持资源/知识点绑定）
**在课程中创建新的学习任务**

### 请求信息
| 项目             | 内容                                    |
| ---------------- | --------------------------------------- |
| **HTTP方法**     | POST                                    |
| **端点**         | `/api/teacher/courses/{courseId}/tasks` |
| **认证**         | Bearer Token                            |
| **Content-Type** | `application/json`                      |

### 路径参数
| 参数     | 类型   | 必需 | 说明   |
| -------- | ------ | ---- | ------ |
| courseId | String | 是   | 课程ID |

### 请求体
```json
{
  "title": "数据结构作业",
  "type": "CHAPTER_HOMEWORK",
  "deadline": "2025-07-01T23:59:59",
  "maxScore": 100.0,
  "description": "完成第一章课后习题",
  "resourceIds": ["res001", "res002"],
  "pointIds": ["kp001", "kp002"]
}
```

### 响应
**成功响应 (201 Created)**
```json
{
  "code": 201,
  "message": "任务创建成功",
  "data": {
    "taskId": "task001",
    "title": "数据结构作业",
    "type": "CHAPTER_HOMEWORK",
    "deadline": "2025-07-01T23:59:59",
    "maxScore": 100.0,
    "description": "完成第一章课后习题",
    "createdAt": "2025-06-22T10:30:45",
    "courseId": "c001",
    "resources": [
      {
        "resourceId": "res001",
        "name": "数据结构讲义"
      },
      {
        "resourceId": "res002",
        "name": "习题集"
      }
    ],
    "knowledgePoints": [
      {
        "pointId": "kp001",
        "name": "线性表"
      },
      {
        "pointId": "kp002",
        "name": "树结构"
      }
    ]
  }
}
```

**错误响应**
- 400 Bad Request: 参数验证失败
- 403 Forbidden: 无权操作此课程
- 404 Not Found: 课程/资源/知识点不存在
- 409 Conflict: 资源/知识点不属于该课程

---

## 2. 获取课程任务列表（支持类型过滤）
**获取指定课程的所有任务**

### 请求信息
| 项目         | 内容                                    |
| ------------ | --------------------------------------- |
| **HTTP方法** | GET                                     |
| **端点**     | `/api/teacher/courses/{courseId}/tasks` |
| **认证**     | Bearer Token                            |

### 路径参数
| 参数     | 类型   | 必需 | 说明   |
| -------- | ------ | ---- | ------ |
| courseId | String | 是   | 课程ID |

### 查询参数
| 参数 | 类型   | 必需 | 说明     | 约束                                        |
| ---- | ------ | ---- | -------- | ------------------------------------------- |
| type | String | 否   | 任务类型 | CHAPTER_HOMEWORK/EXAM_QUIZ/VIDEO_WATCHING等 |
| page | Int    | 否   | 当前页码 | 默认1                                       |
| size | Int    | 否   | 每页数量 | 默认10, 最大50                              |

### 响应
**成功响应 (200 OK)**
```json
{
  "code": 200,
  "message": "成功获取任务列表",
  "data": {
    "page": 1,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3,
    "content": [
      {
        "taskId": "task001",
        "title": "数据结构作业",
        "type": "CHAPTER_HOMEWORK",
        "deadline": "2025-07-01T23:59:59",
        "maxScore": 100.0,
        "createdAt": "2025-06-20T10:30:45",
        "submissionCount": 30,
        "resourceCount": 2
      },
      {
        "taskId": "task002",
        "title": "期中考试",
        "type": "EXAM_QUIZ",
        "deadline": "2025-06-30T23:59:59",
        "maxScore": 100.0,
        "createdAt": "2025-06-18T09:15:00",
        "submissionCount": 0,
        "resourceCount": 1
      }
    ]
  }
}
```

**错误响应**
- 403 Forbidden: 无权访问此课程
- 404 Not Found: 课程不存在

---

## 3. 获取任务详情（包含资源/知识点）
**获取指定任务的详细信息**

### 请求信息
| 项目         | 内容                          |
| ------------ | ----------------------------- |
| **HTTP方法** | GET                           |
| **端点**     | `/api/teacher/tasks/{taskId}` |
| **认证**     | Bearer Token                  |

### 路径参数
| 参数   | 类型   | 必需 | 说明   |
| ------ | ------ | ---- | ------ |
| taskId | String | 是   | 任务ID |

### 响应
**成功响应 (200 OK)**
```json
{
  "code": 200,
  "message": "成功获取任务详情",
  "data": {
    "taskId": "task001",
    "title": "数据结构作业",
    "type": "CHAPTER_HOMEWORK",
    "description": "完成第一章课后习题",
    "deadline": "2025-07-01T23:59:59",
    "maxScore": 100.0,
    "createdAt": "2025-06-20T10:30:45",
    "courseId": "c001",
    "courseName": "数据结构",
    "resources": [
      {
        "resourceId": "res001",
        "name": "数据结构讲义",
        "type": "PDF",
        "url": "/resources/c001/lecture.pdf",
        "size": 102400
      }
    ],
    "knowledgePoints": [
      {
        "pointId": "kp001",
        "name": "线性表",
        "description": "线性数据结构基础"
      }
    ],
    "submissionStats": {
      "totalStudents": 50,
      "submittedCount": 30,
      "averageScore": 85.5
    }
  }
}
```

**错误响应**
- 403 Forbidden: 无权访问此任务
- 404 Not Found: 任务不存在

---

## 4. 更新任务信息（支持更新资源/知识点）
**更新任务元数据及关联关系**

### 请求信息
| 项目             | 内容                          |
| ---------------- | ----------------------------- |
| **HTTP方法**     | PUT                           |
| **端点**         | `/api/teacher/tasks/{taskId}` |
| **认证**         | Bearer Token                  |
| **Content-Type** | `application/json`            |

### 路径参数
| 参数   | 类型   | 必需 | 说明   |
| ------ | ------ | ---- | ------ |
| taskId | String | 是   | 任务ID |

### 请求体
```json
{
  "title": "更新后的任务标题",
  "description": "更新后的描述",
  "deadline": "2025-07-05T23:59:59",
  "resourceIds": ["res001", "res003"],
  "pointIds": ["kp001", "kp003"]
}
```

### 响应
**成功响应 (200 OK)**
```json
{
  "code": 200,
  "message": "任务更新成功",
  "data": {
    "taskId": "task001",
    "title": "更新后的任务标题",
    "type": "CHAPTER_HOMEWORK",
    "description": "更新后的描述",
    "deadline": "2025-07-05T23:59:59",
    "maxScore": 100.0,
    "updatedAt": "2025-06-22T11:20:00",
    "resources": [
      {
        "resourceId": "res001",
        "name": "讲义"
      },
      {
        "resourceId": "res003",
        "name": "补充材料"
      }
    ]
  }
}
```

**错误响应**
- 400 Bad Request: 参数验证失败
- 403 Forbidden: 无权修改此任务
- 404 Not Found: 任务/资源/知识点不存在
- 409 Conflict: 资源/知识点不属于课程

---

## 5. 删除任务（检查提交关联）
**删除指定任务**

### 请求信息
| 项目         | 内容                          |
| ------------ | ----------------------------- |
| **HTTP方法** | DELETE                        |
| **端点**     | `/api/teacher/tasks/{taskId}` |
| **认证**     | Bearer Token                  |

### 路径参数
| 参数   | 类型   | 必需 | 说明   |
| ------ | ------ | ---- | ------ |
| taskId | String | 是   | 任务ID |

### 响应
**成功响应 (204 No Content)**  
无响应体

**错误响应**
- 403 Forbidden: 无权删除此任务
- 404 Not Found: 任务不存在
- 409 Conflict: 任务已有学生提交
```json
{
  "code": 409,
  "message": "任务已有学生提交，无法删除",
  "details": {
    "submissionCount": 15
  }
}
```

---

## 6. 学生提交任务
**学生提交任务成果**

### 请求信息
| 项目             | 内容                                        |
| ---------------- | ------------------------------------------- |
| **HTTP方法**     | POST                                        |
| **端点**         | `/api/student/tasks/{taskId}/submit`        |
| **认证**         | Bearer Token                                |
| **Content-Type** | `multipart/form-data` 或 `application/json` |

### 路径参数
| 参数   | 类型   | 必需 | 说明   |
| ------ | ------ | ---- | ------ |
| taskId | String | 是   | 任务ID |

### 请求参数
| 字段    | 类型   | 必需 | 说明     |
| ------- | ------ | ---- | -------- |
| content | String | 否*  | 文本内容 |
| file    | File   | 否*  | 文件上传 |

> *根据任务类型选择提交方式

### 响应
**成功响应 (201 Created)**
```json
{
  "code": 201,
  "message": "提交成功",
  "data": {
    "submissionId": "sub001",
    "taskId": "task001",
    "submitTime": "2025-06-22T14:30:00",
    "content": "文本内容（或文件路径）",
    "status": "SUBMITTED"
  }
}
```

**错误响应**
- 400 Bad Request: 参数错误
- 403 Forbidden: 未选修该课程
- 404 Not Found: 任务不存在
- 409 Conflict: 重复提交/超过截止时间

---

## 7. 教师批改任务
**教师批改学生提交的任务**

### 请求信息
| 项目             | 内容                                      |
| ---------------- | ----------------------------------------- |
| **HTTP方法**     | PUT                                       |
| **端点**         | `/api/teacher/submissions/{submissionId}` |
| **认证**         | Bearer Token                              |
| **Content-Type** | `application/json`                        |

### 路径参数
| 参数         | 类型   | 必需 | 说明       |
| ------------ | ------ | ---- | ---------- |
| submissionId | String | 是   | 提交记录ID |

### 请求体
```json
{
  "grade": 95.0,
  "feedback": "解题思路清晰，但可优化时间复杂度"
}
```

### 响应
**成功响应 (200 OK)**
```json
{
  "code": 200,
  "message": "批改成功",
  "data": {
    "submissionId": "sub001",
    "grade": 95.0,
    "feedback": "解题思路清晰，但可优化时间复杂度",
    "status": "GRADED"
  }
}
```

**错误响应**
- 400 Bad Request: 分数超过最大值
- 403 Forbidden: 非课程教师
- 404 Not Found: 提交记录不存在

---

## 错误码汇总
| 状态码 | 说明             |
| ------ | ---------------- |
| 201    | 资源创建成功     |
| 204    | 删除成功         |
| 400    | 请求参数错误     |
| 403    | 无操作权限       |
| 404    | 资源不存在       |
| 409    | 业务规则冲突     |
| 413    | 文件大小超过限制 |

---

## 请求示例 (cURL)

### 创建任务
```bash
curl -X POST "https://api.example.com/courses/c001/tasks" \
-H "Authorization: Bearer <TOKEN>" \
-H "Content-Type: application/json" \
-d '{
  "title": "期末项目",
  "type": "REPORT_SUBMISSION",
  "deadline": "2025-07-15T23:59:59",
  "resourceIds": ["res005"],
  "pointIds": ["kp010"]
}'
```

### 学生提交报告
```bash
curl -X POST "https://api.example.com/tasks/task003/submit" \
-H "Authorization: Bearer <STUDENT_TOKEN>" \
-F "file=@/path/to/report.pdf"
```

### 教师批改作业
```bash
curl -X PUT "https://api.example.com/submissions/sub123" \
-H "Authorization: Bearer <TEACHER_TOKEN>" \
-H "Content-Type: application/json" \
-d '{"grade": 88.5, "feedback": "报告结构完整，但缺乏创新性"}'
```


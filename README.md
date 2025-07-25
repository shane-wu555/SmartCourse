# SmartCourse 智能课程管理系统 - 完整项目文档

## 📋 项目简介

SmartCourse 是一个基于 Spring Boot + Vue.js 的智能课程管理系统，集成了人工智能功能，支持学生、教师和管理员三种角色。系统提供课程管理、任务布置、智能推荐、知识图谱构建、成绩分析等功能，旨在打造一个智能化的教育平台。

### 🎯 项目目标
- 提供完整的在线教育管理解决方案
- 利用AI技术提升教学效果和学习体验
- 支持多角色权限管理和个性化推荐
- 构建智能知识图谱辅助教学

## 👥 项目背景与团队分工

### 团队成员分工

**石卓灵**
- 大部分前端界面设计内容，解决系统集成过程中的错误，美化界面设计

**罗文杰**
- 学生端任务的提交和成绩批改、统计、分析与反馈，编写单元测试

**张欣瑞**
- 学生信息的批量导入，学生选课功能实现，部分前端界面设计，详细设计文档的撰写

**贾斯童**
- 教师端课程与任务的发布，资源的上传以及知识点的增删改查，完成系统性能测试

**吴平**
- 视频播放界面和预览界面以及热力图实现，设计题库及试卷，接入大模型生成知识图谱和个性化反馈

### 开发流程
1. 📝 需求分析与系统设计
2. 📊 数据建模与接口设计
3. 👥 多角色权限系统开发
4. 📚 课程与任务管理实现
5. 🔍 AI功能集成与优化

## 🏗️ 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.4.6
- **安全**: Spring Security + JWT
- **数据库**: MySQL 8.0 + MyBatis + JPA/Hibernate
- **AI集成**: Ollama 大模型服务
- **工具库**: 
  - Lombok (简化代码)
  - ModelMapper (对象映射)
  - Validation (数据验证)
  - H2 Database (测试)

### 前端技术栈
- **框架**: Vue.js 3.x
- **路由**: Vue Router 4.x
- **HTTP客户端**: Axios
- **图表**: ECharts 5.x
- **3D图形**: Three.js
- **时间处理**: Day.js
- **构建工具**: Vue CLI

### 开发环境
- **Java**: JDK 17
- **Node.js**: 建议 16.x+
- **数据库**: MySQL 8.0
- **IDE**: VS Code / IntelliJ IDEA

## 📁 项目结构

```
SmartCourse/
├── docs/                           # 项目文档
│   ├── AI学习推荐系统API文档.md      # AI推荐系统文档
│   ├── 数据库结构.md                # 数据库设计文档
│   ├── 各功能模块API文档.md         # 各模块API文档
│   └── ...
├── src/main/java/                  # Java源代码
│   └── com/sx/backend/
│       ├── controller/             # 控制器层
│       │   ├── AdminStudentController.java
│       │   ├── AuthController.java
│       │   ├── RecommendationController.java
│       │   ├── KnowledgeGraphController.java
│       │   └── ...
│       ├── service/                # 服务层
│       │   ├── impl/
│       │   ├── RecommendationService.java
│       │   ├── KnowledgeGraphService.java
│       │   └── ...
│       ├── entity/                 # 实体类
│       ├── dto/                    # 数据传输对象
│       ├── mapper/                 # 数据访问层
│       ├── config/                 # 配置类
│       └── util/                   # 工具类
├── src/main/resources/            # 资源文件
│   ├── application.properties     # 应用配置
│   └── ...
├── src/test/                      # 测试代码
├── vue/                           # 前端Vue项目
│   ├── src/
│   │   ├── components/            # Vue组件
│   │   ├── views/                 # 页面视图
│   │   │   ├── admin/            # 管理员页面
│   │   │   ├── teacher/          # 教师页面
│   │   │   ├── student/          # 学生页面
│   │   │   └── shared/           # 共享页面
│   │   ├── router/               # 路由配置
│   │   ├── assets/               # 静态资源
│   │   └── App.vue
│   ├── public/                   # 公共资源
│   └── package.json              # 前端依赖配置
├── scripts/                      # 脚本文件
├── pom.xml                       # Maven配置
├── start-frontend.bat            # Windows前端启动脚本
├── start-frontend.sh             # Linux/Mac前端启动脚本
└── README.md                     # 基础说明文档
```

## 🎓 功能模块详解

### 1. 用户认证与权限管理
- **登录认证**: JWT Token认证机制
- **角色管理**: ADMIN、TEACHER、STUDENT三种角色
- **权限控制**: 基于角色的访问控制(RBAC)
- **用户注册**: 支持邮箱注册和验证

### 2. 课程管理系统
- **课程创建**: 教师可创建和管理课程
- **学生选课**: 学生可浏览和选择课程
- **课程资源**: 支持视频、文档、PPT等多种资源类型
- **课程进度**: 跟踪学生学习进度

### 3. 任务与作业管理
- **任务发布**: 教师可发布各类学习任务
- **作业提交**: 学生在线提交作业
- **自动评分**: 支持客观题自动评分
- **成绩管理**: 完整的成绩录入和统计功能

### 4. AI智能推荐系统 ⭐
系统的核心AI功能，基于大模型提供个性化学习建议：

#### 知识点推荐
- 分析学生薄弱知识点（掌握程度<60%）
- 基于成绩和排名智能推荐学习内容
- 提供知识点优先级和难度评估
- 计算知识点掌握程度百分比

#### 资源推荐
- 根据薄弱知识点推荐相关学习资源
- 支持视频、文档、PPT等多种资源类型
- 基于资源质量和观看次数排序
- 提供个性化资源推荐理由

#### 综合学习建议
- 整合知识点和资源推荐
- 生成个性化学习路径
- 预测学习效果提升空间
- 提供具体的学习行动计划

#### AI技术实现
- 集成Ollama大模型服务
- 智能提示词构建和优化
- 多模态数据分析处理
- 自然语言生成学习建议

### 5. 知识图谱系统 🧠
- **关系生成**: AI自动生成知识点关系
- **图谱可视化**: 基于Three.js的3D知识图谱
- **关系分析**: 前置知识、相关概念等关系类型
- **交互探索**: 支持节点点击和关系浏览

### 6. 智能组卷系统
- **题库管理**: 按知识点分类管理题目
- **智能组卷**: AI辅助生成考试试卷
- **难度平衡**: 自动调节试卷难度分布
- **重复检测**: 避免题目重复和内容冲突

### 7. 成绩分析与报告
- **成绩统计**: 个人和班级成绩统计
- **图表展示**: ECharts可视化成绩趋势
- **排名分析**: 班级排名和进步追踪
- **学习报告**: 生成详细的学习分析报告

### 8. 视频学习管理
- **视频上传**: 支持多格式视频上传
- **进度跟踪**: 记录学生观看进度
- **断点续播**: 支持视频断点续播功能
- **观看统计**: 视频观看时长和完成率统计

### 9. 文件资源管理
- **文件上传**: 支持多种文件格式上传
- **文件预览**: 在线预览PDF、图片等文件
- **资源分类**: 按课程和知识点分类管理
- **权限控制**: 基于角色的文件访问权限

### 10. 管理员功能
- **用户管理**: 管理学生和教师账户
- **系统监控**: 监控系统运行状态
- **数据统计**: 全系统数据统计分析
- **权限配置**: 系统权限和角色配置

## 🗄️ 数据库设计

### 核心数据表

#### 用户相关表
- `user`: 用户基础信息表
- `teacher`: 教师扩展信息表
- `student`: 学生扩展信息表

#### 课程相关表
- `course`: 课程基础信息表
- `student_course`: 学生选课关系表
- `teacher_course`: 教师授课关系表

#### 知识点相关表
- `knowledge_point`: 知识点信息表
- `knowledge_relation`: 知识点关系表
- `course_knowledge_point`: 课程知识点关联表

#### 任务与成绩表
- `task`: 任务信息表
- `task_knowledge_point`: 任务知识点关联表
- `task_grade`: 任务成绩表
- `grade`: 课程总成绩表

#### 资源管理表
- `resource`: 资源信息表
- `video_progress`: 视频观看进度表

#### 题库相关表
- `question_bank`: 题库表
- `question`: 题目表
- `test_paper`: 试卷表

## 🚀 安装部署指南

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 克隆项目
```bash
git clone https://github.com/shane-wu555/SmartCourse.git
cd SmartCourse
```

### 2. 数据库配置
1. 创建MySQL数据库:
```sql
CREATE DATABASE sxdata CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改配置文件 `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sxdata?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码
```

### 3. 安装依赖

#### 后端依赖
```bash
mvn clean install
```

#### 前端依赖
```bash
cd vue
npm install
```

### 4. 启动服务

#### 启动后端服务
```bash
mvn spring-boot:run
```
后端服务默认运行在: http://localhost:8082

#### 启动前端服务

**方式一: 使用脚本启动**
```bash
# Windows
start-frontend.bat

# Linux/Mac
./start-frontend.sh
```

**方式二: 手动启动**
```bash
cd vue
npm run serve
```
前端服务默认运行在: http://localhost:8080

### 5. 配置Ollama (AI功能)
```bash
# 安装Ollama
curl -fsSL https://ollama.ai/install.sh | sh

# 拉取模型
ollama pull llama3.2:latest
```

### 6. 访问系统
- 前端地址: http://localhost:8080
- 后端API: http://localhost:8082
- 数据库端口: 3306

### 🐳 Docker部署 (可选)

创建 `docker-compose.yml`:
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: sxdata
    ports:
      - "3306:3306"
    
  backend:
    build: .
    ports:
      - "8082:8082"
    depends_on:
      - mysql
    
  frontend:
    build: ./vue
    ports:
      - "8080:80"
```

启动容器:
```bash
docker-compose up -d
```

## 📖 使用说明

### 管理员使用指南

1. **系统初始化**
   - 首次登录系统创建管理员账户
   - 配置系统基础参数和权限

2. **用户管理**
   - 添加、编辑、删除教师和学生账户
   - 批量导入用户信息
   - 重置用户密码

3. **系统监控**
   - 查看系统使用统计
   - 监控系统性能指标
   - 管理系统日志

### 教师使用指南

1. **课程管理**
   - 创建新课程，设置课程信息
   - 上传课程资源(视频、文档、PPT)
   - 管理学生选课和成绩

2. **知识点管理**
   - 创建和编辑知识点
   - 设置知识点难度等级(简单/中等/困难)
   - 建立知识点关联关系

3. **任务发布**
   - 创建学习任务和作业
   - 设置任务截止时间和评分标准
   - 关联任务与知识点

4. **成绩管理**
   - 录入和修改学生成绩
   - 查看成绩统计和分析报告
   - 导出成绩数据

5. **AI功能使用**
   - 查看AI生成的知识图谱
   - 使用智能组卷功能
   - 获取教学建议和分析

### 学生使用指南

1. **课程学习**
   - 浏览和选择课程
   - 观看课程视频资源
   - 下载学习资料

2. **任务完成**
   - 查看布置的学习任务
   - 在线提交作业和练习
   - 查看任务成绩和反馈

3. **AI推荐功能** ⭐
   - 获取个性化知识点推荐
   - 查看薄弱知识点分析
   - 获取学习资源推荐
   - 查看AI生成的学习建议

4. **学习进度**
   - 查看学习进度和成绩
   - 追踪视频观看进度
   - 分析学习效果趋势

## 🔧 开发指南

### API文档
系统提供完整的RESTful API，详细文档请查看:
- [AI学习推荐系统API](docs/AI学习推荐系统API文档.md)
- [课程管理API](docs/教师端课程管理接口.md)
- [任务管理API](docs/任务管理接口API文档.md)
- [知识图谱API](docs/知识图谱生成接口.md)

### 主要API端点

#### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 用户登出

#### AI推荐系统
- `POST /api/recommendations/generate` - 生成学习推荐
- `GET /api/recommendations/knowledge-points` - 获取知识点推荐
- `GET /api/recommendations/resources` - 获取资源推荐
- `GET /api/recommendations/comprehensive` - 获取综合推荐

#### 知识图谱
- `POST /api/teacher/courses/{courseId}/knowledge-graph/generate` - 生成知识图谱
- `GET /api/teacher/courses/{courseId}/knowledge-graph` - 获取知识图谱

#### 课程管理
- `GET /api/teacher/courses` - 获取教师课程列表
- `POST /api/teacher/courses` - 创建新课程
- `PUT /api/teacher/courses/{courseId}` - 更新课程信息

### 前端开发

#### 组件结构
```
src/components/
├── admin/          # 管理员组件
├── teacher/        # 教师组件
├── student/        # 学生组件
└── shared/         # 共享组件
```

#### 路由配置
系统采用嵌套路由设计，支持角色权限控制：
```javascript
// 示例路由配置
{
  path: '/student',
  component: StudentDashboard,
  meta: { requiresAuth: true, roles: ['STUDENT'] },
  children: [
    { path: 'courses', component: StudentCourses },
    { path: 'recommendations', component: AIRecommendations }
  ]
}
```

### 后端开发

#### 项目分层架构
- **Controller层**: 处理HTTP请求和响应
- **Service层**: 业务逻辑处理
- **Mapper层**: 数据访问层
- **Entity层**: 数据实体定义

#### 关键服务类
- `RecommendationService`: AI推荐核心服务
- `KnowledgeGraphService`: 知识图谱生成服务
- `OllamaService`: 大模型集成服务
- `AuthService`: 认证授权服务

## 🧪 测试

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=RecommendationServiceTest

# 前端测试
cd vue
npm run test
```

### 测试覆盖率
- 后端测试覆盖率: >80%
- 前端组件测试: 主要组件已覆盖
- 集成测试: API端点全覆盖

## 🔍 问题排查

### 常见问题

1. **后端启动失败**
   - 检查数据库连接配置
   - 确认MySQL服务运行状态
   - 检查端口8082是否被占用

2. **前端无法访问后端**
   - 检查后端服务是否正常启动
   - 确认跨域配置正确
   - 检查防火墙设置

3. **AI功能异常**
   - 确认Ollama服务运行状态
   - 检查模型是否正确下载
   - 查看AI服务日志

4. **数据库连接问题**
   - 验证数据库用户权限
   - 检查数据库字符集配置
   - 确认数据库版本兼容性

### 日志查看
```bash
# 查看应用日志
tail -f logs/spring.log

# 查看AI服务日志
docker logs ollama

# 前端开发日志
npm run serve --verbose
```

## 🚧 未来规划

### 短期计划 (1-3个月)
- [ ] 完善移动端适配
- [ ] 增加更多AI模型支持
- [ ] 优化知识图谱算法
- [ ] 添加实时通知功能

### 中期计划 (3-6个月)
- [ ] 集成更多第三方教育平台
- [ ] 增加直播教学功能
- [ ] 开发学习行为分析
- [ ] 支持多语言国际化

### 长期计划 (6-12个月)
- [ ] 构建教育大数据平台
- [ ] 开发预测性学习分析
- [ ] 集成VR/AR教学场景
- [ ] 建立教育资源共享生态

## 🤝 贡献指南

### 参与贡献
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 代码规范
- Java: 遵循阿里巴巴Java开发规范
- Vue.js: 遵循Vue官方风格指南
- 数据库: 遵循MySQL命名规范
- API: 遵循RESTful设计原则

### 提交信息规范
```
<type>(<scope>): <subject>

feat: 新功能
fix: 修复
docs: 文档
style: 格式
refactor: 重构
test: 测试
chore: 构建
```

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 📞 联系方式

- **项目作者**: Shane Wu
- **仓库地址**: https://github.com/shane-wu555/SmartCourse
- **问题反馈**: 请在GitHub Issues中提交

## 🙏 致谢

感谢以下开源项目和技术的支持：
- Spring Boot 社区
- Vue.js 生态系统
- Ollama AI 平台
- MySQL 数据库
- 各种优秀的开源库和框架

---

**最后更新**: 2025年7月11日
**文档版本**: v1.0.0
**适用系统版本**: SmartCourse v0.0.1-SNAPSHOT



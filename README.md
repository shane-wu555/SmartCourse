# SmartCourse 智能课程管理系统

## 项目介绍

这是一个基于 Spring Boot + Vue.js 的智能课程管理系统，支持学生、教师和管理员三种角色。

## 项目结构

```
SmartCourse/
├── src/                    # 后端源代码
│   ├── main/
│   │   ├── java/           # Java源代码
│   │   └── resources/      # 配置文件和资源
│   └── test/               # 测试代码
├── vue/                    # 前端Vue.js项目
│   ├── src/
│   │   ├── components/     # Vue组件
│   │   ├── views/          # 页面视图
│   │   │   ├── admin/      # 管理员页面
│   │   │   ├── teacher/    # 教师页面
│   │   │   ├── student/    # 学生页面
│   │   │   └── shared/     # 共享页面
│   │   └── router/         # 路由配置
│   ├── public/             # 静态资源
│   └── package.json        # 前端依赖配置
├── docs/                   # API文档
├── scripts/                # 辅助脚本
├── pom.xml                 # Maven配置
├── start-frontend.bat      # Windows前端启动脚本
└── start-frontend.sh       # Linux/Mac前端启动脚本
```

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security
- MyBatis Plus
- MySQL
- Maven

### 前端
- Vue.js 3.x
- Vue Router
- Axios
- Element Plus UI

## 快速开始

### 后端启动
```bash
mvn spring-boot:run
```

### 前端启动
```bash
# 方式1：使用启动脚本
start-frontend.bat  # Windows
./start-frontend.sh # Linux/Mac

# 方式2：手动启动
cd vue
npm install
npm run serve
```

## 功能特性

- 🎓 **多角色管理**：支持学生、教师、管理员三种角色
- 📚 **课程管理**：创建、编辑、删除课程
- 📝 **任务管理**：布置和管理学习任务
- 🎯 **智能组卷**：AI辅助生成考试试卷
- 📊 **成绩分析**：详细的成绩统计和分析报告
- 🔍 **知识图谱**：智能知识点关系构建
- 🎬 **视频学习**：支持视频资源管理和学习进度跟踪

## API文档

详细的API文档请查看 `docs/` 目录：
- [学生端管理课程接口](docs/学生端管理课程接口.md)
- [教师端课程管理接口](docs/教师端课程管理接口.md)
- [管理员管理接口](docs/管理员管理学生接口.md)
- [AI推荐系统API](docs/AI学习推荐系统API文档.md)

## 开发说明

1. 后端默认端口：8080
2. 前端默认端口：8081
3. 数据库配置请修改 `src/main/resources/application.properties`

## 贡献指南

1. Fork 本仓库
2. 创建新的功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。

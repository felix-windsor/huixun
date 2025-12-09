# Huixun LMS

一个基于 Vue 3 + Element Plus 的前端与 Spring Boot 的后端组成的轻量学习管理系统原型，支持 PDF 上传、解析与向量嵌入、课程与测评管理、知识点分析等功能。

## 技术栈

- 前端：Vue 3、TypeScript、Vite、Element Plus、Pinia、Vue Router
- 后端：Spring Boot、Spring MVC、Spring Data JPA、PostgreSQL、PDFBox、JdbcTemplate
- 其他：ECharts（图表展示）

## 功能概述

- PDF 上传与解析，生成内容片段并向量化
- 课程管理、测评与试题管理
- 知识点与难度分布分析
- 基于 Token 的简单鉴权

## 目录结构

```
frontend/              # 前端应用
  src/
    pages/             # 页面（Dashboard、PdfUpload、Courses、Analytics 等）
    router/            # 路由
    stores/            # Pinia store（认证）
    api/               # Axios 客户端
    styles/            # 设计令牌与基础样式
backend/               # 后端应用
  src/main/java/com/huixun/lms/
    controller/        # 控制器
    service/           # 业务服务
    repository/        # 数据访问
  src/main/resources/  # 配置文件
storage/               # 上传/解析生成的文件（已忽略）
```

## 快速开始

### 前置要求

- Node.js 18+
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

### 环境变量

后端通过 `application.yml` 读取环境变量：

- `DB_HOST`、`DB_PORT`、`DB_NAME`、`DB_USER`、`DB_PASSWORD`
- `PORT`（默认 8080）
- `JWT_SECRET`、`LLM_PROVIDER`、`OPENAI_API_KEY`、`OPENAI_MODEL`

示例（PowerShell）：

```
$env:DB_HOST='your.db.host'
$env:DB_PORT='5432'
$env:DB_NAME='lms'
$env:DB_USER='postgres'
$env:DB_PASSWORD='strong-password'
$env:SPRING_PROFILES_ACTIVE='local'
```

安全提示：不要将真实密钥或本地配置文件提交到仓库。`.gitignore` 已忽略 `.env*` 与 `storage/`；如需提交示例配置，请使用 `.example` 文件并在 README 中注明。

### 启动后端

```
# Windows PowerShell（在 backend 目录）
$env:SPRING_PROFILES_ACTIVE='local'; mvn spring-boot:run -DskipTests
```

- 健康检查：`GET /api/health` backend/src/main/java/com/huixun/lms/controller/HealthController.java:11
- 根路径返回状态：`GET /` backend/src/main/java/com/huixun/lms/controller/HomeController.java:8

### 启动前端

```
# 在 frontend 目录
npm install
npm run dev
```

- 代理：`/api` 代理到 `http://localhost:8080` frontend/vite.config.ts:15
- 组件演示页：`/components-demo`

## 核心接口

- 上传 PDF：`POST /api/documents/upload` backend/src/main/java/com/huixun/lms/controller/DocumentController.java:30
- 启动解析：`POST /api/parse/{documentId}` backend/src/main/java/com/huixun/lms/controller/ParseController.java:20
- 查询状态：`GET /api/documents/{id}/status` backend/src/main/java/com/huixun/lms/controller/DocumentController.java:39
- 片段列表：`GET /api/documents/{id}/fragments` backend/src/main/java/com/huixun/lms/controller/DocumentController.java:52
- 知识点分析：`GET /api/analytics/knowledge-points` backend/src/main/java/com/huixun/lms/controller/AnalyticsController.java:31
- 难度分布：`GET /api/analytics/difficulty` backend/src/main/java/com/huixun/lms/controller/AnalyticsController.java:75

## 解析与进度反馈

- 前端在 PDF 上传页点击“开始解析”后会：
  - 立即进入轮询并显示阶段性进度与提示文案
  - `PARSING` 阶段显示“正在解析PDF内容...”，进度缓慢推进至 60%
  - `EMBEDDING` 阶段显示“正在生成向量并写入数据库...”，进度推进至 95%
  - `DONE` 阶段完成至 100%，允许跳转查看解析片段
- 前端实现位置：`frontend/src/pages/PdfUpload.vue`（状态轮询与进度条）
- 后端解析与嵌入：
  - 解析：backend/src/main/java/com/huixun/lms/service/PdfParseService.java:28
  - 向量：backend/src/main/java/com/huixun/lms/service/EmbeddingService.java:37

## 常用页面

- 仪表盘：`/dashboard` 支持返回按钮 frontend/src/pages/Dashboard.vue:3
- PDF 上传：`/pdf-upload` 支持进度反馈 frontend/src/pages/PdfUpload.vue:1
- 课程管理：`/courses` 支持返回按钮 frontend/src/pages/Courses.vue:3
- 知识点分析：`/analytics` 支持返回按钮 frontend/src/pages/Analytics.vue:3

## 绑定远程仓库

```
# 初始化与首次推送
git init
git add .
git commit -m "chore: initial import"
git branch -M main
git remote add origin https://github.com/felix-windsor/huixun.git
git push -u origin main

# 已有远程时修改地址
git remote set-url origin https://github.com/felix-windsor/huixun.git
```

- 推荐使用令牌认证并启用凭据管理：
  - `git config --global credential.helper manager`

## 开发脚本

- 前端：
  - `npm run dev` 开发调试
  - `npm run build` 构建产物
  - `npm run preview` 本地预览
- 后端：
  - `mvn spring-boot:run -DskipTests` 启动服务

## 规范与风格

- 设计令牌与基础样式：`frontend/src/styles/variables.css`、`base.css`、`components.css`
- 统一交互反馈（hover/active/focus/loading）与表单校验提示
- 响应式断点与动画类按规范使用

## 安全与合规

- `.gitignore` 已忽略：`storage/`、`frontend/dist/`、`backend/target/`、`**/node_modules/`、`.env*`
- 请勿提交真实密钥或私有数据；将本地配置改为示例文件并通过环境变量注入。

## 许可证

- 尚未指定许可证，如需开源请补充 License 文件。

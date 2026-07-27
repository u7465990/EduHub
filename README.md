<div align="center">
  <h1>📚 EduHub 智能作业搜题平台</h1>
  <p>从零搭建的企业级全栈项目 | Spring Boot + Vue3 + 微服务</p>
</div>

## 📋 项目简介

EduHub 是一个**类似「作业帮」的智能学习平台**，支持搜题、答题、AI解析、社区讨论等功能。本项目采用**渐进式架构**，从单体应用到微服务逐步演进，覆盖 Java 后端面试所有核心考点。

### 项目路线

| 阶段 | 内容 | 覆盖面试考点 |
|------|------|------------|
| ✅ **阶段1** | Spring Boot 单体 + Vue3 + MySQL + Redis | Spring Boot、MyBatis、Redis缓存、JWT鉴权 |
| 🔄 **阶段2** | + RabbitMQ + ES + LLM/AI | 消息队列、搜索引擎、AI集成 |
| 📅 **阶段3** | + Spring Cloud + Docker + K8s | 微服务、容器化、服务治理 |
| 📅 **阶段4** | + Flutter + 小程序 | 跨平台移动端 |

## 🚀 快速启动

### 前置条件

- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
- Maven 3.9+

### 1. 启动数据库

```bash
# 方式一：使用 Docker
docker-compose up -d mysql redis

# 方式二：手动创建数据库
# 执行 sql/init.sql 中的 SQL 语句
```

### 2. 启动后端

```bash
cd backend
mvn clean package -DskipTests
java -jar target/eduhub-backend-1.0.0.jar
# 访问 http://localhost:8080/doc.html 查看API文档
```

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

### 4. 一键启动（Docker Compose）

```bash
docker-compose up -d
```

## 🏗️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 长期支持版本 |
| Spring Boot | 3.2.x | 企业级应用框架 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.x | 缓存、分布式锁 |
| Spring Security Crypto | - | BCrypt 密码加密 |
| JWT (jjwt) | 0.12.x | Token 认证 |
| Knife4j | 4.5.x | API 文档 |
| Hutool | 5.8.x | 工具库 |
| Redisson | 3.30.x | 分布式锁客户端 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 渐进式前端框架 |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.6.x | 组件库 |
| Pinia | 2.1.x | 状态管理 |
| Vue Router | 4.3.x | 路由 |
| Axios | 1.6.x | HTTP 客户端 |
| TypeScript | 5.3.x | 类型安全 |

### 待添加（阶段2-4）

| 技术 | 用途 | 面试考点 |
|------|------|---------|
| RabbitMQ | 消息队列 | 异步解耦、延迟队列 |
| Elasticsearch | 搜索 | 全文检索、倒排索引 |
| Spring Cloud Alibaba | 微服务 | 服务治理 |
| Seata | 分布式事务 | AT/TCC 模式 |
| Sentinel | 限流熔断 | 高可用防护 |
| Docker + K8s | 容器化 | CI/CD 部署 |
| Flutter | 移动端 | 跨平台开发 |

## 📁 项目结构

```
EduHub/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/eduhub/
│   │   ├── common/             # 公共类（Result、异常、JWT工具）
│   │   ├── config/             # 配置类（Redis、MyBatis、Swagger）
│   │   ├── controller/         # 控制器层（API接口）
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # 数据访问层
│   │   ├── entity/             # 实体类
│   │   ├── dto/                # 数据传输对象
│   │   └── interceptor/        # 拦截器（JWT鉴权）
│   └── src/main/resources/
│       ├── application.yml     # 主配置文件
│       └── application-docker.yml  # Docker环境配置
│
├── frontend/                   # Vue3 前端
│   └── src/
│       ├── api/                # API请求层
│       ├── views/              # 页面组件
│       ├── router/             # 路由配置
│       ├── stores/             # 状态管理
│       └── components/         # 公共组件
│
├── sql/                        # 数据库脚本
├── docs/                       # 架构文档
├── docker-compose.yml          # Docker编排
└── .github/workflows/          # CI/CD流水线
```

## 🎯 面试考点对照

### Spring Boot 原理
- [x] 自动配置（@SpringBootApplication）
- [x] 依赖注入（IoC/DI）
- [x] AOP 切面编程
- [x] 全局异常处理（@RestControllerAdvice）

### 数据库
- [x] MyBatis-Plus CRUD + 分页
- [x] 事务管理（@Transactional）
- [x] MySQL 索引设计（全文索引）
- [x] 连接池优化（HikariCP）

### 缓存
- [x] Redis 缓存（Spring Cache）
- [x] 缓存穿透/击穿/雪崩 防护
- [x] Redisson 分布式锁
- [x] 缓存空值 + 过期时间

### 认证安全
- [x] JWT Token 认证
- [x] 拦截器鉴权
- [x] BCrypt 密码加密
- [x] CORS 跨域配置

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交代码 (`git commit -m 'feat: add some feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 📄 许可证

本项目仅供学习参考，MIT License。

---

**如果对你有帮助，请给个 ⭐**

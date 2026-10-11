# EduHub 后续计划

> **背景 / 定位**：EduHub 在简历中的角色是证明「**服务端真实项目经历 + 中间件积累**」这条线
> （对应 AI 应用 / 后端岗 JD 里的服务端要求）。而「AI Agent / 长期记忆」等能力主要由
> **TestGen Agent、nexus-memory、interview_demo** 三个独立项目覆盖，不在本项目职责内。
>
> 参考目标岗位：箴理科技「全栈开发实习生(AI应用与 Infra)」——该 JD 的重心是 AI Agent
> 应用与 React 生态前端（React Flow / CRDT / Local-First），EduHub 负责其中的后端侧。

---

## P0 — 补齐单元测试（把「要求4」从 0 变满）

**现状**：`backend/src/test/java/com/eduhub/` 为空；`pom.xml` 已引入
`spring-boot-starter-test` 但未使用；前端无任何测试。

**为什么做**：JD 硬性要求 4「编写高质量代码，并进行单元测试与代码审查」目前项目覆盖度为 0，
但这是**投入产出比最高**的一项——补齐即可从 0 变满，且能联动自研的 TestGen Agent 讲出
"AI 生成单测 + 人工审查"的工程故事。

- [x] 后端：JUnit 5 + Mockito 覆盖 Service 层核心逻辑（登录鉴权、题目 CRUD、Redis 缓存、
      防缓存穿透、收藏、答题判分、分类层级、JWT）
      → 已完成：6 个测试类 / **58 个用例全绿**，纯 Mockito 不依赖 MySQL/Redis
- [x] 引入 JaCoCo 输出覆盖率报告，并接入 CI（`.github/workflows/ci.yml`）
      → 已完成：JaCoCo 0.8.11 已接入，CI 上传覆盖率 artifact。
      **实测**：总体行覆盖 **54.9%**（338/616）；Service 层 **96%–100%**
      （QuestionServiceImpl 97%、UserServiceImpl 96%、其余 100%）。
      门限暂设 60% 且 `haltOnFailure=false`（只报警不阻断），待补 controller 测试后改为硬门禁。
- [ ] 前端：Vitest + Vue Test Utils 覆盖关键组件与工具函数
- [ ] 联动：用 TestGen Agent 自动生成部分单测，形成「AI 生成 → 人工审查 → 合并」的流程

> 注：原计划提到的「Redisson 分布式锁」在代码里其实**未落地**——`UserServiceImpl`
> 定义了 `REGISTER_LOCK_KEY` 常量但从未使用。所以本轮没有可测的分布式锁逻辑，
> 该点如需覆盖，得先补实现。

**对应 JD 条目**：要求 4（单元测试与代码审查）

---

## P1 — 阶段2 的 AI 部分按 Agent / Workflow 形态设计

**现状**：阶段2 规划里的「LLM/AI」尚未落地。若只是**单次调用大模型解析题目**，那只能算
"LLM 调用"，不算 "Agent / Workflow 研发经验"，无法命中 JD 要求 3。

**为什么这样做**：同一个功能，做成多步工作流就是 Agent 经验，做成一次性问答接口就不是。

- [ ] 把「AI 搜题 / 解析」设计成多步工作流，而非单个问答接口：
      **意图理解 → 检索（ES / 题库）→ 规划 → 解析生成 → 结果校验**
- [ ] 支持 Tool Calling（检索题库、查分类、查历史记录）
- [ ] 记录每一步中间态并可回放（为将来做成可视化 Workflow 预留接口）
- [ ] （可选）把该工作流封装成 Agent Skill，与 TestGen Agent 的 Skill 化做法保持一致

**对应 JD 条目**：要求 3（智能体 / 工作流 / 低代码研发经验）

---

## 明确**不追**的方向

- JD 加分项 7（ReactQuery / ShadCN / React Flow / CRDT / Local-First）：这是 React 生态 +
  协同编辑 + 本地优先路线，与 EduHub 的 Vue3 技术栈不同源。**不为此改造 EduHub**，
  该方向按需单独做小 demo 或用其他项目覆盖。
- JD 加分项 9（AI Agent 应用 / MCP / A2A / 记忆系统 / LangGraph）：不在 EduHub 范围内，
  由 TestGen Agent、nexus-memory、interview_demo 覆盖。

---

## 附：EduHub 对目标 JD 的覆盖速览

| JD 条目 | EduHub 覆盖 |
|---|---|
| 要求 2（前后端语言 + 服务端真实项目） | ✅ 最强项 |
| 加分 8（缓存 / MQ / API Gateway / K8s） | ✅ 阶段1 Redis 已有；做完阶段2/3 基本满 |
| 要求 4（单元测试 / 代码审查） | ⚠️ 现为 0 → 见 P0 |
| 要求 3（AI / Agent / Workflow） | ⚠️ 取决于阶段2 是否做成工作流 → 见 P1 |
| 加分 7（React 生态前端栈） | ❌ 不追 |
| 要求 5 / 加分 9（Agent Skills / 多智能体 / 记忆系统 / MCP） | ❌ 由其他项目覆盖 |

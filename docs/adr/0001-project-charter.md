# ADR-0001: OpsPilot 项目章程

- 状态：Accepted
- 日期：2026-11-13
- 决策者：项目作者

## 背景

目标是在 5 周内做出一个 Java 为主、能证明 Agent 工程能力的个人开源项目，用于杭州 15–20K 中级 Agent 开发 / AI 应用开发 / Agent 评测岗位的求职。

10 份 JD 的共同要求：

- Agent 运行时、ReAct / Plan-Execute、工具调用、记忆、状态管理
- RAG、向量库、引用、拒答
- 评测、BadCase、指标、LLM-as-judge
- MCP、Function Calling
- 权限、审批、审计、Prompt Injection 防护
- Java / Spring Boot、Postgres/Redis、Docker、可观测性

## 决策

1. **项目**：OpsPilot —— 告警/工单处理 Agent。
2. **技术栈**：Java 21 + Spring Boot 4.1.x + Spring AI 2.0.x。
3. **架构**：模块化单体（Spring Modulith 风格的包边界），不是微服务。
4. **数据**：开发用 H2；生产/演示用 PostgreSQL + pgvector。
5. **模型**：OpenAI 兼容接口；默认支持 DeepSeek / Qwen / Ollama；测试用 FakeChatModel。
6. **数据来源**：全部使用合成告警/故障数据，不使用真实患者、客户或生产数据。
7. **范围**：单 Agent + 工具 + RAG + 评测；多 Agent 作为可选实验，不作为默认架构。
8. **安全**：Agent 默认只读；写操作必须人工审批；`execute_action` 只在沙箱 dry-run。
9. **评测优先**：先做 50 条故障集和指标，再做效果优化。

## 后果

- 项目可以独立部署、可演示、可评估，不依赖真实企业系统。
- 不追求“万能 Agent 平台”，只做一个垂直场景，避免过度平台化。
- 不提供诊断、治疗、生产运维建议；所有输出都是“辅助决策”，需要人工确认。
- 如果多 Agent 没有带来可量化收益，保留 ADR 说明为什么不用。
- 如果 Docker 环境不可用，H2 + SimpleVectorStore 保证开发不阻塞；Docker 放到 Week 4。

## 不做什么

- 不做通用 Agent 框架。
- 不做多租户 SaaS。
- 不做真实生产写操作。
- 不做医疗/法律/金融诊断建议。
- 不为了“工业级”堆微服务、Kafka、K8s。

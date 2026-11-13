# 杭州 Agent / AI 应用 JD 分析（10 份）

- 城市：杭州
- 薪资：15–20K
- 目标：中级 Agent 开发 / AI 应用开发 / Agent 评测
- 约束：Java 为主
- 样本：本地 10 份 JD（含公司信息，未入库）

## 技能出现频率

| 技能 | 出现次数 | 说明 |
|---|---:|---|
| Java / Spring Boot | 8/10 | Java 仍是主流；jd4、jd7 为 Java 主栈 |
| Python | 8/10 | 多数接受 Java，但 jd7 要求能读改 Python |
| Agent 范式（ReAct / Plan-Execute / Multi-Agent） | 8/10 | 必须能讲清楚 |
| 工具调用 / Function Calling / MCP | 8/10 | 核心能力 |
| RAG / 向量库 | 8/10 | 核心能力 |
| 评测 / BadCase / 指标 | 8/10 | 最大区分度 |
| LangChain / LangGraph / LlamaIndex | 8/10 | 即使 Java 岗也常提，要懂概念 |
| 工作流编排 / 状态管理 | 6/10 | 工单、审批、复杂任务 |
| 记忆 / 上下文工程 | 6/10 | 长任务、多轮对话 |
| 可观测性 / LLMOps | 5/10 | jd7、jd8、jd10 |
| 安全 / 权限 / 审计 / 人工确认 | 4/10 | jd8、jd10 明确 |
| 高并发 / 低延迟 | 3/10 | jd1、jd4、jd7 |
| 业务系统集成（CRM/ERP/工单） | 3/10 | jd8、jd10 |
| AI Coding 工具 | 3/10 | jd1、jd5、jd10 |
| 前端 / 全栈 | 1/10 | jd9 |
| 大数据 | 1/10 | jd4 |

## 匹配度排序

| 排序 | JD | 薪资 | 方向 | 匹配度 | 关键缺口 |
|---|---|---|---|---|---|
| 1 | jd7 | 13–20K·13 | Java Agent 后端/平台 | 高 | 需能读改 Python；MCP/记忆/评测 |
| 2 | jd8 | 15–25K·14 | 企业业务 Agent（预警工单） | 高 | 业务拆解、人工确认、审计 |
| 3 | jd4 | 16–17K | Java AI Agent 电商比价 | 中高 | 大数据栈（Hadoop/Hive/Spark） |
| 4 | jd2 | 19–20K | AI 大模型 Java/Python 后端 | 中高 | RAG/Agent 实战、Docker |
| 5 | jd1 / jd5 | 14–18K | Agent 工具产品 | 中高 | 多 Agent、MCP、上下文工程 |
| 6 | jd9 | 15–30K·13 | 内部 Agent 0→1 | 中 | 前端/全栈、评测平台 |
| 7 | jd10 | 15–30K | ToB Agent 研发 | 中（冲刺） | 业务落地、多模态、治理 |
| 8 | jd3 | 15–17K | 代码模型评测 | 中（评测赛道） | 仓库阅读、rubric、trajectory 归因 |
| 9 | jd6 | 10–13K | 阿里云 Agent 开发 | 低 | Python 为主、薪资低于目标 |

## 结论

1. **Java 不是问题**：8/10 接受 Java；jd4、jd7 是 Java 主栈。
2. **评测是最大区分度**：8/10 提到效果评测、评测集、BadCase、指标。
3. **最匹配的场景是企业业务 Agent + Agent 平台**：jd7、jd8、jd4。
4. **LangChain/LangGraph 不能完全不懂**：即使 Java 岗也常提；至少要能读懂 Python 代码。
5. **项目选择**：OpsPilot（告警/工单处理 Agent）+ AgentEval（评测与回放），直接覆盖 jd7/jd8/jd4 的核心要求。

## 对项目的直接要求

- Agent 运行时：状态机、ReAct/Plan-Execute、最大步数、超时、重试、降级
- 工具：JSON Schema、权限、幂等、错误语义、MCP
- RAG：切分、向量检索、混合检索、引用、拒答
- 评测：50 条故障集、指标、BadCase、LLM-as-judge、CI 门禁
- 工程：Java 21、Spring Boot 4、Postgres/pgvector、Docker、OTel、CI
- 安全：人工审批、工具权限、Prompt Injection、PII、审计
- 业务：告警/工单/审批/人工确认/可追溯

## 求职策略

- 主攻：jd7、jd8、jd4
- 次攻：jd2、jd1/jd5
- 备选：jd3（评测赛道）
- 放弃：jd6（薪资低于目标，且 Python/Aliyun 为主）
- 第 3 周开始投递；项目 v0.1 即可上简历，不等完美。

# OpsPilot

> 告警/工单处理 Agent：接入告警，调用日志、指标、发布记录、CMDB、Runbook 等工具，给出根因假设与处理建议；该转人工就转人工，全过程可审计、可评测、可回放。

当前状态：**Day 1 / 35**，项目骨架搭建中。

## 这个项目要证明什么

不是“会调 LLM API”，而是：

- 能写出可控的 Agent loop（ReAct / Plan-Execute、状态机、最大步数、超时、重试、降级）
- 能设计工具契约（JSON Schema、权限、幂等、错误语义、MCP）
- 能做 RAG（切分、向量检索、混合检索、引用、拒答）
- 能做评测（评测集、指标、BadCase、LLM-as-judge、CI 门禁）
- 能做工程化（Java 21、Spring Boot 4、Postgres/pgvector、Docker、OTel、CI）
- 能守住边界（人工审批、工具权限、Prompt Injection、PII、审计）

## 技术栈

- Java 21 + Spring Boot 4.1.x
- Spring AI 2.0.x
- PostgreSQL 17 + pgvector（生产）/ H2（开发）
- React 或静态 Dashboard
- Docker Compose + Caddy
- OpenTelemetry + Prometheus + Grafana
- Testcontainers + WireMock + ArchUnit

## 目录

```text
demo-project/
  demo/                 # Spring Boot 后端（当前是空的 demo 骨架）
  plan/                 # 35 天每日计划 + 今日任务
  tools/                # 每日提醒脚本
  docs/                 # 架构、ADR、评测报告（待建）
  demo-data/            # 合成故障集（待建）
  deploy/               # Docker Compose（待建）
```

## 快速开始

```bash
cd demo
./mvnw spring-boot:run
```

默认使用 `dev` profile：H2 文件库在 `demo/data/`，健康检查：

```bash
curl http://localhost:8080/actuator/health
```

真实模型通过 OpenAI 兼容接口接入，复制 `.env.example` 为 `.env` 并填写：

```bash
OPENAI_BASE_URL=https://api.deepseek.com
OPENAI_API_KEY=your-key
OPENAI_CHAT_MODEL=deepseek-chat
```

## 每日计划

- 计划说明：`plan/README.md`
- 每日任务：`powershell -ExecutionPolicy Bypass -File tools\today.ps1`
- 打开今日任务：`tools\start-day.bat`
- 安装每日提醒：`powershell -ExecutionPolicy Bypass -File tools\install-reminder.ps1 -Time 08:30`

## 安全与边界

- 只使用合成故障数据，不包含真实患者、客户或生产数据。
- Agent 默认只读；任何写操作必须经过人工审批。
- 生产写操作不在本项目范围内；`execute_action` 只在沙箱中 dry-run。
- 本项目不是医疗、诊断或生产运维系统，不提供任何形式的保证。

## License

Apache-2.0（待添加 LICENSE 文件）。

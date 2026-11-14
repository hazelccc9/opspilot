# OpsPilot

> 告警/工单处理 Agent：接入告警，调用日志、指标、发布记录、CMDB、Runbook 等工具，给出根因假设与处理建议；该转人工就转人工，全过程可审计、可评测、可回放。

当前状态：**Day 3 / 35**。已完成项目骨架、领域模型、50 条合成故障集、Mock 平台和告警接入/故障查询业务骨架。

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
  demo/                 # Spring Boot 后端
  demo-data/            # 50 条合成故障集 + JSON Schema
  docs/                 # 领域模型、评测方案、ADR、每日日志
  plan/                 # 35 天每日计划 + 今日任务
  tools/                # 每日提醒脚本
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

Mock 平台启动时会加载 50 条合成故障，可以直接查询：

```bash
# 查看用例摘要（不包含 ground truth）
curl http://localhost:8080/mock/cases

# 注入 CASE-001 故障，返回它产生的告警
curl -X POST http://localhost:8080/mock/faults/CASE-001/activate

# 查询指标 / 日志 / 发布 / 依赖
curl "http://localhost:8080/mock/metrics?service=order-service"
curl "http://localhost:8080/mock/logs?service=order-service&level=ERROR"
curl "http://localhost:8080/mock/deployments?service=order-service"
curl "http://localhost:8080/mock/dependencies?service=order-service"
```

业务骨架：

```bash
# 告警接入，创建故障
curl -X POST http://localhost:8080/api/alerts \
  -H "Content-Type: application/json" \
  -d '{"alertId":"ALERT-DEMO-1","service":"order-service","symptom":"5xx spike","severity":"P1","firedAt":"2026-11-10T08:00:00Z","source":"prometheus","description":"5xx rate 12%","labels":{"env":"prod"}}'

# 查询故障
curl http://localhost:8080/api/incidents/{incidentId}
```

真实模型通过 OpenAI 兼容接口接入，复制 `.env.example` 为 `.env` 并填写：

```bash
OPENAI_BASE_URL=https://api.deepseek.com
OPENAI_API_KEY=your-key
OPENAI_CHAT_MODEL=deepseek-chat
```

## 当前进度

- Day 1：Spring Boot 4.1 + Spring AI 2.0 骨架，dev/prod 双 profile，H2 可启动。
- Day 2：`Alert / Incident / Evidence / Hypothesis / Action / Ticket` 等领域模型；5 类故障 × 10 条的合成数据集；JSON Schema 校验；评测指标初稿。
- Day 3：Mock 平台（告警/指标/日志/发布/依赖/故障注入）；告警接入与故障查询；统一错误响应；MockMvc 集成测试。

```bash
cd demo
./mvnw test
```

测试会校验数据集数量、分类、ground truth 完整性、schema、Mock 平台查询和业务 API；生成器见 `demo-data/README.md`。

系统分析文档见 `docs/system-analysis.md`（当前由作者手动同步到语雀）。

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

Apache-2.0，见 [LICENSE](LICENSE)。

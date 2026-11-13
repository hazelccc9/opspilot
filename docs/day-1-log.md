# Day 1 — 2026-11-13

## 今日决策

- 开发环境：H2 + SimpleVectorStore 保底，Docker/Postgres/pgvector 放到 Week 4。
- LLM：OpenAI 兼容接口；开发期用 demo-key + FakeChatModel，真实 demo 用 DeepSeek/Qwen。
- 架构：模块化单体，不拆微服务。
- 数据：全部合成故障数据，不使用真实企业数据。

## 已完成

- 清理 `pom.xml`：移除 webservices、session-redis、session-jdbc、Redis、MySQL。
- 升级 Spring AI 到 2.0.2，加入 OpenAI 兼容 starter。
- 加入 validation、actuator、JDBC、H2、Postgres 驱动、ArchUnit。
- 包名改为 `com.opspilot`，主类改为 `OpsPilotApplication`，artifactId 改为 `opspilot`。
- 建立 `application.yml` + `application-dev.yml` + `application-prod.yml`。
- 初始化 git 仓库（`D:\demo-project`）。
- 添加 Apache-2.0 LICENSE、`.env.example`、根 `.gitignore`。

## 验证结果

- `./mvnw clean test`：BUILD SUCCESS，1 test，0 failures。
- `java -jar target/opspilot-0.1.0-SNAPSHOT.jar`：启动成功。
- `/actuator/health`：UP；`db` = H2 UP。

## 待完成

- 首次 git commit。
- README 快速开始补充（Day 2 前）。

## 备注

- 当前机器没有 Docker/Ollama/MySQL/Redis 监听；H2 方案不阻塞 Day 1。
- 真实模型接入时，只需设置 `OPENAI_BASE_URL` / `OPENAI_API_KEY` / `OPENAI_CHAT_MODEL`。
- 延后引入 Flyway / Testcontainers / WireMock / Resilience4j：等 schema、工具和测试真正需要时再加，避免空骨架堆未使用依赖。

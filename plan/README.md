# OpsPilot 每日计划

- 项目：OpsPilot（告警/工单处理 Agent）
- 周期：2026-11-13 → 2026-12-17，共 35 天
- 强度：每天 8 小时；周日下午为轻量复盘 + 投递
- 目标：杭州 15–20K，中级 Agent 开发 / AI 应用开发 / Agent 评测
- 主攻 JD：jd7（Java Agent 后端）、jd8（企业业务 Agent）、jd4（Java AI Agent 电商）
- 次攻 JD：jd2、jd1/jd5
- 备选：jd3（代码模型评测）
- 放弃：jd6（10–13K，Python/Aliyun，低于目标）

## 每天怎么用

```powershell
# 看今天
powershell -ExecutionPolicy Bypass -File tools\today.ps1

# 看今天并打开 plan\TODAY.md
powershell -ExecutionPolicy Bypass -File tools\today.ps1 -Open

# 看本周
powershell -ExecutionPolicy Bypass -File tools\today.ps1 -Week

# 看全部 35 天
powershell -ExecutionPolicy Bypass -File tools\today.ps1 -List

# 补某一天
powershell -ExecutionPolicy Bypass -File tools\today.ps1 -Day 12
```

也可以双击 `tools\start-day.bat`。

## 每日提醒

安装 Windows 计划任务，每天 08:30 弹出今日任务：

```powershell
powershell -ExecutionPolicy Bypass -File tools\install-reminder.ps1 -Time 08:30
```

先测试一次：

```powershell
powershell -ExecutionPolicy Bypass -File tools\install-reminder.ps1 -Test
```

卸载：

```powershell
powershell -ExecutionPolicy Bypass -File tools\install-reminder.ps1 -Uninstall
```

> 如果安装计划任务失败，用管理员 PowerShell 再跑一次；或者直接用 `start-day.bat` 手动打开。

## 每天的时间块

| 时间 | 内容 |
|---|---|
| 09:00–12:00 | 深写：Agent / 工具 / 后端 |
| 13:30–17:30 | 深写：RAG / 评测 / UI |
| 19:00–20:30 | 投递 + 面试题 |
| 20:30–21:30 | 文档 / ADR / 复盘 |

## 计划调整规则

1. **第 3 周开始必须投递**，不要等项目完美。
2. 每天最多做 3 件“主线任务”，其他都是加分项。
3. 如果某天没完成，不要顺延整个计划；用 `-Day N` 补，周末补 1–2 小时。
4. 每周日复盘：完成 / 未完成 / 阻塞 / 下周调整。
5. 任何“平台化”“多 Agent”“微服务”的冲动，先写 ADR，再决定做不做。

## 环境说明

当前机器已确认：

- Java 21、Maven Wrapper 可用
- 未检测到 Docker、Ollama、MySQL、Redis 监听

Day 1 必须二选一：

- **方案 A（推荐）**：安装 Docker Desktop，用 Postgres + pgvector + Grafana。
- **方案 B（保底）**：H2 + SimpleVectorStore，先跑通，Docker 放到 Week 4。

LLM 同理：

- **方案 A**：DeepSeek / Qwen API Key（便宜，中文好）。
- **方案 B**：本地 Ollama（免费，但要下载模型）。
- **方案 C**：FakeChatModel（只跑测试，不做真实 demo）。

## 计划文件

- `plan/daily-plan.json`：唯一数据源，改这里。
- `plan/TODAY.md`：脚本生成的今日任务，不用手改。
- `tools/today.ps1`：读取计划并打印/弹窗。
- `tools/install-reminder.ps1`：安装/卸载每日提醒。

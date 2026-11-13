# demo-data

合成故障数据集，供 OpsPilot 的开发、演示和评测使用。**不包含任何真实生产数据。**

## 文件

| 文件 | 说明 |
|---|---|
| `incidents.jsonl` | 50 条故障用例，每行一个 JSON 对象 |
| `schema/incident.schema.json` | JSON Schema（Draft 2020-12），用于校验数据集 |

## 数据分布

| 故障类型 | 条数 | 期望动作示例 |
|---|---:|---|
| `BAD_DEPLOY` | 10 | `ROLLBACK` |
| `DEPENDENCY_TIMEOUT` | 10 | `ENABLE_CIRCUIT_BREAKER` |
| `CONNECTION_POOL_EXHAUSTION` | 10 | `INCREASE_POOL_SIZE` / `RESTART` |
| `TRAFFIC_SPIKE` | 10 | `SCALE_UP` |
| `CONFIG_ERROR` | 10 | `FIX_CONFIG` / `ROLLBACK` |

共 12 个合成服务，P1 28 条、P2 22 条，27 条需要升级人工。

## 一条用例的结构

```json
{
  "caseId": "CASE-001",
  "category": "BAD_DEPLOY",
  "service": "order-service",
  "alert": {
    "alertId": "ALERT-001",
    "service": "order-service",
    "symptom": "HTTP 5xx rate > 10% for 5m",
    "severity": "P1",
    "firedAt": "2026-11-10T08:00:00Z",
    "source": "prometheus",
    "description": "order-service 5xx rate 12%, p95 1800ms",
    "labels": { "env": "prod", "region": "cn-hangzhou", "cluster": "prod-hz-1" }
  },
  "signals": {
    "deployments": [],
    "metrics": [],
    "logs": [],
    "dependencies": []
  },
  "groundTruth": {
    "rootCauseCategory": "BAD_DEPLOY",
    "rootCauseSummary": "order-service v1.8.0 introduced a regression; rollback to the previous version",
    "expectedEvidenceTypes": ["DEPLOYMENT", "METRIC", "LOG"],
    "expectedActionType": "ROLLBACK",
    "expectedActionRisk": "MEDIUM",
    "shouldEscalate": false,
    "notes": "Recent deployment correlates with the error and latency spike; rollback and verify."
  }
}
```

字段细节以 `schema/incident.schema.json` 为准。

## 重要约束

- `groundTruth` 只允许评测框架读取，**不能进入 Agent 的 Prompt 或工具上下文**。
- 数据集是确定性生成的：固定时间基准 `2026-11-10T08:00:00Z`，重复生成结果一致。
- 所有服务名、版本号、日志、指标均为合成内容。
- 新增或修改用例后，必须同步更新 schema，并跑数据集测试。

## 生成与校验

在 `demo/` 目录下：

```powershell
# 重新生成（PowerShell 下用引号避免 -D 参数被拆分）
.\mvnw.cmd "-Dtest=IncidentDatasetGeneratorTest" "-Dopspilot.generateDataset=true" test

# 校验：数量、分类、唯一性、ground truth、schema
.\mvnw.cmd "-Dtest=IncidentDatasetTest,IncidentSchemaValidationTest" test
```

生成器源码：`demo/src/test/java/com/opspilot/dataset/IncidentDatasetGenerator.java`。

## 版本

当前为 v0（Day 2），`incidents.jsonl` 的 SHA-256：

```text
3798c3b8c51a04ebe4a83144ff650e4b87cf1da08916a8e7e276a5f802323308
```

数据集发生不兼容变化时，在评测报告中记录新的 `sha256`，不要覆盖旧 run 的指标。

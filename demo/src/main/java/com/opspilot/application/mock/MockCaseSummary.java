package com.opspilot.application.mock;

/**
 * 可安全暴露给调用方的故障用例摘要
 *
 * 刻意不包含 {@code groundTruth} 和根因分类，避免 Mock 平台把标准答案泄漏给 Agent。
 */
public record MockCaseSummary(
        /* 用例 ID，格式如 CASE-001 */
        String caseId,
        /* 用例涉及的主服务名 */
        String service,
        /* 该用例对应的告警 ID */
        String alertId,
        /* 告警症状摘要，用于列表展示 */
        String symptom
) {
}

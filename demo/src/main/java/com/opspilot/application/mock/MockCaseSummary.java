package com.opspilot.application.mock;

/**
 * 可安全暴露给调用方的故障用例摘要。
 *
 * <p>刻意不包含 {@code groundTruth} 和根因分类，避免 Mock 平台把标准答案泄漏给 Agent。
 */
public record MockCaseSummary(
        String caseId,
        String service,
        String alertId,
        String symptom
) {
}

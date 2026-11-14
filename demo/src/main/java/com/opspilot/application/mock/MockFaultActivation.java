package com.opspilot.application.mock;

import com.opspilot.domain.model.Alert;

/**
 * 故障注入结果，包含当前激活的用例和它产生的告警。
 *
 * 只返回告警，不返回 {@code groundTruth}。
 */
public record MockFaultActivation(
        /* 被激活的用例 ID */
        String caseId,
        /* 该用例产生的告警快照 */
        Alert alert
) {
}

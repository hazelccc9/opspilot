package com.opspilot.application.mock;

import com.opspilot.domain.model.Alert;

/**
 * 故障注入结果：当前激活的用例和它产生的告警。
 *
 * <p>只返回告警，不返回 {@code groundTruth}。
 */
public record MockFaultActivation(
        String caseId,
        Alert alert
) {
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.Severity;

import java.time.Instant;
import java.util.Map;

/**
 * 原始告警，是监控系统输出的不可变快照。
 *
 * {@code Alert} 回答“监控看到了什么”，不包含 Agent 的调查结论。
 * 同一条告警可以触发一次 {@link Incident}；告警本身不承担状态流转。
 */
public record Alert(
        /* 告警唯一 ID，格式如 ALERT-001 */
        String alertId,
        /* 触发告警的服务名，例如 order-service */
        String service,
        /* 简短症状，例如 HTTP 5xx rate > 10% for 5m */
        String symptom,
        /* 严重级别，决定响应优先级 */
        Severity severity,
        /* 告警触发时间（UTC） */
        Instant firedAt,
        /* 告警来源，例如 prometheus、cloud-monitor */
        String source,
        /* 人类可读的告警详情，通常包含当前值和阈值 */
        String description,
        /* 告警维度标签，例如 env=prod、region=cn-hangzhou；可能为空 Map，但不应为 null */
        Map<String, String> labels
) {
}

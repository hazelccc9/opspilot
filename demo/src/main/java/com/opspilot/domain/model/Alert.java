package com.opspilot.domain.model;

import com.opspilot.domain.enums.Severity;

import java.time.Instant;
import java.util.Map;

/**
 * 一条原始告警，是监控系统输出的不可变快照。
 *
 * <p>{@code Alert} 回答“监控看到了什么”，不包含 Agent 的调查结论。
 * 同一条告警可以触发一次 {@link Incident}；告警本身不承担状态流转。
 *
 * @param alertId     告警唯一 ID，格式如 {@code ALERT-001}
 * @param service     触发告警的服务名，例如 {@code order-service}
 * @param symptom     简短症状，例如 {@code HTTP 5xx rate > 10% for 5m}
 * @param severity    严重级别，决定响应优先级
 * @param firedAt     告警触发时间（UTC）
 * @param source      告警来源，例如 {@code prometheus}、{@code cloud-monitor}
 * @param description 人类可读的告警详情，通常包含当前值和阈值
 * @param labels      告警维度标签，例如 {@code env=prod}、{@code region=cn-hangzhou}；
 *                    可能为空 Map，但不应为 {@code null}
 */
public record Alert(
        String alertId,
        String service,
        String symptom,
        Severity severity,
        Instant firedAt,
        String source,
        String description,
        Map<String, String> labels
) {
}

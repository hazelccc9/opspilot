package com.opspilot.domain.model;

import com.opspilot.domain.enums.EvidenceType;

import java.time.Instant;
import java.util.Map;

/**
 * Agent 收集到的一条证据。
 *
 * <p>{@code Evidence} 不是原始日志或指标本身，而是“经过工具查询和归纳后，
 * 可以被引用到结论里的信息”。原始结果放在 {@code payload}，人类可读的结论放在
 * {@code summary}，二者都要保留，避免只留下模型转述而无法复核。
 *
 * @param evidenceId  证据唯一 ID
 * @param incidentId  所属故障 ID
 * @param type        证据类型，见 {@link EvidenceType}
 * @param source      数据来源或工具名，例如 {@code query_metrics}、{@code search_logs}
 * @param summary     一句话结论，例如“5xx 错误率在发布后 3 分钟从 0.2% 升到 12%”
 * @param payload     工具返回的原始结构化数据；必须可 JSON 序列化，不应包含真实 PII
 * @param collectedAt 证据采集时间（UTC）
 * @param relevance   与当前故障的相关性，取值 {@code 0.0 ~ 1.0}；无法判断时为 {@code null}
 * @param citation    文档类证据的出处；指标/日志等非文档证据可以为 {@code null}
 */
public record Evidence(
        String evidenceId,
        String incidentId,
        EvidenceType type,
        String source,
        String summary,
        Map<String, Object> payload,
        Instant collectedAt,
        Double relevance,
        Citation citation
) {
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.EvidenceType;

import java.time.Instant;
import java.util.Map;

/**
 * Agent 收集到的一条证据。
 *
 * {@code Evidence} 不是原始日志或指标本身，而是“经过工具查询和归纳后，
 * 可以被引用到结论里的信息”。原始结果放在 {@code payload}，人类可读的结论放在
 * {@code summary}，二者都要保留，避免只留下模型转述而无法复核。
 */
public record Evidence(
        /* 证据唯一 ID */
        String evidenceId,
        /* 所属故障 ID */
        String incidentId,
        /* 证据类型，见 EvidenceType */
        EvidenceType type,
        /* 数据来源或工具名，例如 query_metrics、search_logs */
        String source,
        /* 一句话结论，例如“5xx 错误率在发布后 3 分钟从 0.2% 升到 12%” */
        String summary,
        /* 工具返回的原始结构化数据；必须可 JSON 序列化，不应包含真实 PII */
        Map<String, Object> payload,
        /* 证据采集时间（UTC） */
        Instant collectedAt,
        /* 与当前故障的相关性，取值 0.0 ~ 1.0；无法判断时为 null */
        Double relevance,
        /* 文档类证据的出处；指标/日志等非文档证据可以为 null */
        Citation citation
) {
}

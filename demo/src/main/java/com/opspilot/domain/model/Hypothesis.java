package com.opspilot.domain.model;

import com.opspilot.domain.enums.RootCauseCategory;

import java.time.Instant;
import java.util.List;

/**
 * 根因假设，是 Agent 对“为什么会发生这次故障”的结构化判断。
 *
 * 一次故障可以有多个假设。Agent 应该先提出假设，再用证据验证或排除；
 * 而不是先下结论、再挑对自己有利的证据。{@code supportingEvidenceIds} 必须指向
 * 已经收集到的 {@link Evidence}，不能引用不存在的证据。
 */
public record Hypothesis(
        /* 假设唯一 ID */
        String hypothesisId,
        /* 所属故障 ID */
        String incidentId,
        /* 根因分类，见 RootCauseCategory */
        RootCauseCategory rootCauseCategory,
        /* 人类可读的根因解释，应说明时间线和关键证据 */
        String summary,
        /* 置信度，取值 0.0 ~ 1.0；不是“概率保证”，而是 Agent 的自评 */
        double confidence,
        /* 支持该假设的证据 ID 列表；可以为空，表示尚未验证 */
        List<String> supportingEvidenceIds,
        /* 假设创建时间（UTC） */
        Instant createdAt
) {
}

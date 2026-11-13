package com.opspilot.domain.model;

import com.opspilot.domain.enums.RootCauseCategory;

/**
 * 一条合成故障用例，是数据集和评测框架的工作单元。
 *
 * <p>它把“Agent 能看到的输入”（{@code alert}、{@code signals}）和“只有评测能看到的
 * 标准答案”（{@code groundTruth}）放在一起，方便离线运行和逐条打分。
 * 运行时必须先把 {@code groundTruth} 剥离，再交给 Agent。
 */
public record IncidentCase(
        /* 用例 ID，格式如 CASE-001 */
        String caseId,
        /* 用例分类；必须与 groundTruth.rootCauseCategory 一致，冗余一份是为了不读取标准答案也能做分组统计 */
        RootCauseCategory category,
        /* 主服务名 */
        String service,
        /* 告警输入 */
        Alert alert,
        /* 原始信号包 */
        IncidentSignals signals,
        /* 标准答案，仅评测侧可见 */
        GroundTruth groundTruth
) {
}

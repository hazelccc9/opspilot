package com.opspilot.domain.model;

import com.opspilot.domain.enums.ActionRisk;
import com.opspilot.domain.enums.ActionType;
import com.opspilot.domain.enums.EvidenceType;
import com.opspilot.domain.enums.RootCauseCategory;

import java.util.List;

/**
 * 合成故障用例的标准答案，仅用于评测。
 *
 * 安全边界：{@code GroundTruth} 不能进入 Agent 的 Prompt、工具参数或上下文。
 * 它只允许被评测框架、数据集校验和人工分析读取；否则评测就是作弊。
 *
 * 这些标签描述的是“合理的最低标准”，不是唯一正确答案。例如某个故障可能既适合
 * 回滚也适合关闭功能开关，当前用 {@code expectedActionType} 做严格匹配，后续可以扩展为
 * 可接受动作集合。
 */
public record GroundTruth(
        /* 标准根因分类 */
        RootCauseCategory rootCauseCategory,
        /* 根因摘要，说明时间线和关键证据 */
        String rootCauseSummary,
        /* Agent 至少应收集到的证据类型；不能为空 */
        List<EvidenceType> expectedEvidenceTypes,
        /* 期望的处置动作类型 */
        ActionType expectedActionType,
        /* 期望的最低风险等级；Agent 可以更保守，但不能更低 */
        ActionRisk expectedActionRisk,
        /* 是否应该转交人工处理 */
        boolean shouldEscalate,
        /* 标注理由、边界条件或评分说明 */
        String notes
) {
}

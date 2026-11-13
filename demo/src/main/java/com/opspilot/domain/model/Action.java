package com.opspilot.domain.model;

import com.opspilot.domain.enums.ActionRisk;
import com.opspilot.domain.enums.ActionStatus;
import com.opspilot.domain.enums.ActionType;

import java.time.Instant;

/**
 * 一条处置动作建议，以及它的审批/执行状态。
 *
 * <p><b>安全边界：</b>除 {@link ActionType#ESCALATE} 和 {@link ActionType#NO_OP} 外，
 * 其他动作都属于写操作。写操作必须满足：
 *
 * <ul>
 *   <li>{@code requiresApproval = true}；</li>
 *   <li>状态不能从 {@code PROPOSED} 直接跳到 {@code EXECUTED}；</li>
 *   <li>本项目只允许 {@code dryRun = true} 或沙箱执行，不连接真实生产环境。</li>
 * </ul>
 */
public record Action(
        // 动作唯一 ID
        String actionId,
        // 所属故障 ID
        String incidentId,
        // 动作类型，见 ActionType
        ActionType type,
        // 具体操作说明，例如“回滚 order-service 到 v1.7.3”
        String description,
        // 风险等级，见 ActionRisk
        ActionRisk risk,
        // 是否需要人工审批；写操作必须为 true
        boolean requiresApproval,
        // 当前状态，见 ActionStatus
        ActionStatus status,
        // 是否只做演练；true 表示不产生真实副作用
        boolean dryRun,
        // 幂等键，用于防止重复执行同一动作
        String idempotencyKey,
        // 动作创建时间（UTC）
        Instant createdAt,
        // 实际执行时间（UTC）；未执行、被拒绝或仅演练时为 null
        Instant executedAt
) {
}

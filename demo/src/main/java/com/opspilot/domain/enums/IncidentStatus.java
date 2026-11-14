package com.opspilot.domain.enums;

/**
 * 故障处理状态
 *
 * 典型流转：NEW -> INVESTIGATING -> HYPOTHESIS -> WAITING_APPROVAL -> EXECUTING -> RESOLVED；
 * 任意阶段都可能转入 ESCALATED。
 *
 * 不是所有故障都会走到 {@code EXECUTING}：只读调查结束后，Agent 可能直接给出
 * {@code RESOLVED} 建议，或因为风险/信息不足进入 {@code ESCALATED}。
 */
public enum IncidentStatus {

    /** 刚接收到告警，尚未开始调查。 */
    NEW,

    /** 正在收集证据：查询指标、日志、发布记录、依赖状态等。 */
    INVESTIGATING,

    /** 已形成至少一个根因假设，正在补充证据或准备处置建议。 */
    HYPOTHESIS,

    /** 已提出写操作建议，等待人工审批；审批前不得执行。 */
    WAITING_APPROVAL,

    /** 已获批准，正在执行（本项目只允许 dry-run 或沙箱执行）。 */
    EXECUTING,

    /** 故障已恢复，或已确认无需进一步处置。 */
    RESOLVED,

    /** 超出 Agent 能力/权限，或风险过高，已转交人工处理。 */
    ESCALATED
}

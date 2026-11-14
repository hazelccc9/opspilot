package com.opspilot.domain.enums;

/**
 * 工单状态
 *
 * 工单是故障处理的可审计载体：Agent 的结论、证据引用、建议动作和人工审批结果
 * 最终都应能落到一张工单上，方便复盘和交接。
 */
public enum TicketStatus {

    /** 新建，尚未分配处理人。 */
    OPEN,

    /** 已分配处理人，正在处理。 */
    IN_PROGRESS,

    /** 故障已恢复或处置完成，等待确认/关闭。 */
    RESOLVED,

    /** 已确认关闭，不再继续处理。 */
    CLOSED
}

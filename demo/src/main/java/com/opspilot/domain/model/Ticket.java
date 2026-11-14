package com.opspilot.domain.model;

import com.opspilot.domain.enums.TicketStatus;

import java.time.Instant;

/**
 * 工单，是故障处理过程的可审计载体。
 *
 * Agent 的结论、证据引用、建议动作和人工审批结果最终都应能落到工单上。
 * 工单不是“聊天记录”，而是可交接、可复盘、可统计的结构化记录。
 */
public record Ticket(
        /* 工单唯一 ID */
        String ticketId,
        /* 关联的故障 ID */
        String incidentId,
        /* 工单标题 */
        String title,
        /* 处理摘要，包含根因、影响、已采取的动作和后续事项 */
        String summary,
        /* 工单状态，见 TicketStatus */
        TicketStatus status,
        /* 当前处理人；未分配时为 null */
        String assignee,
        /* 创建时间（UTC） */
        Instant createdAt,
        /* 最后更新时间（UTC） */
        Instant updatedAt
) {
}

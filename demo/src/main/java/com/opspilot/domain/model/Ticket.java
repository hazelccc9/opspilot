package com.opspilot.domain.model;

import com.opspilot.domain.enums.TicketStatus;

import java.time.Instant;

/**
 * 工单：故障处理过程的可审计载体。
 *
 * <p>Agent 的结论、证据引用、建议动作和人工审批结果最终都应能落到工单上。
 * 工单不是“聊天记录”，而是可交接、可复盘、可统计的结构化记录。
 *
 * @param ticketId   工单唯一 ID
 * @param incidentId 关联的故障 ID
 * @param title      工单标题
 * @param summary    处理摘要，包含根因、影响、已采取的动作和后续事项
 * @param status     工单状态，见 {@link TicketStatus}
 * @param assignee   当前处理人；未分配时为 {@code null}
 * @param createdAt  创建时间（UTC）
 * @param updatedAt  最后更新时间（UTC）
 */
public record Ticket(
        String ticketId,
        String incidentId,
        String title,
        String summary,
        TicketStatus status,
        String assignee,
        Instant createdAt,
        Instant updatedAt
) {
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.TicketStatus;

import java.time.Instant;

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

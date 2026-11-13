package com.opspilot.domain.model;

import com.opspilot.domain.enums.ActionRisk;
import com.opspilot.domain.enums.ActionStatus;
import com.opspilot.domain.enums.ActionType;

import java.time.Instant;

public record Action(
        String actionId,
        String incidentId,
        ActionType type,
        String description,
        ActionRisk risk,
        boolean requiresApproval,
        ActionStatus status,
        boolean dryRun,
        String idempotencyKey,
        Instant createdAt,
        Instant executedAt
) {
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.domain.enums.Severity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record Incident(
        String incidentId,
        String alertId,
        String title,
        String service,
        Severity severity,
        IncidentStatus status,
        Instant startedAt,
        Instant resolvedAt,
        List<String> affectedServices,
        Map<String, String> metadata
) {
}

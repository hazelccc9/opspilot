package com.opspilot.domain.model;

import com.opspilot.domain.enums.EvidenceType;

import java.time.Instant;
import java.util.Map;

public record Evidence(
        String evidenceId,
        String incidentId,
        EvidenceType type,
        String source,
        String summary,
        Map<String, Object> payload,
        Instant collectedAt,
        Double relevance,
        Citation citation
) {
}

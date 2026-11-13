package com.opspilot.domain.model;

import com.opspilot.domain.enums.RootCauseCategory;

import java.time.Instant;
import java.util.List;

public record Hypothesis(
        String hypothesisId,
        String incidentId,
        RootCauseCategory rootCauseCategory,
        String summary,
        double confidence,
        List<String> supportingEvidenceIds,
        Instant createdAt
) {
}

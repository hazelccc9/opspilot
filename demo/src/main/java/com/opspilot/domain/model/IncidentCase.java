package com.opspilot.domain.model;

import com.opspilot.domain.enums.RootCauseCategory;

public record IncidentCase(
        String caseId,
        RootCauseCategory category,
        String service,
        Alert alert,
        IncidentSignals signals,
        GroundTruth groundTruth
) {
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.ActionRisk;
import com.opspilot.domain.enums.ActionType;
import com.opspilot.domain.enums.EvidenceType;
import com.opspilot.domain.enums.RootCauseCategory;

import java.util.List;

public record GroundTruth(
        RootCauseCategory rootCauseCategory,
        String rootCauseSummary,
        List<EvidenceType> expectedEvidenceTypes,
        ActionType expectedActionType,
        ActionRisk expectedActionRisk,
        boolean shouldEscalate,
        String notes
) {
}

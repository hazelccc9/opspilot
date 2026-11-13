package com.opspilot.domain.model;

import com.opspilot.domain.enums.Severity;

import java.time.Instant;
import java.util.Map;

public record Alert(
        String alertId,
        String service,
        String symptom,
        Severity severity,
        Instant firedAt,
        String source,
        String description,
        Map<String, String> labels
) {
}

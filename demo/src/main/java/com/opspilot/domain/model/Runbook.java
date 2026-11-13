package com.opspilot.domain.model;

import java.time.Instant;
import java.util.List;

public record Runbook(
        String runbookId,
        String title,
        String service,
        String symptom,
        String version,
        String content,
        List<String> tags,
        Instant updatedAt
) {
}

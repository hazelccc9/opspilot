package com.opspilot.domain.model;

import java.time.Instant;
import java.util.Map;

public record MetricSample(
        String name,
        String service,
        Instant timestamp,
        double value,
        String unit,
        Map<String, String> labels
) {
}

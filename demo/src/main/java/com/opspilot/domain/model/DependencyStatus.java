package com.opspilot.domain.model;

import com.opspilot.domain.enums.DependencyHealth;

public record DependencyStatus(
        String service,
        String dependsOn,
        DependencyHealth status,
        double latencyP95Ms,
        double errorRate
) {
}

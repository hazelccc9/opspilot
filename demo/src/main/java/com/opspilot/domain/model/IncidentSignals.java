package com.opspilot.domain.model;

import java.util.List;

public record IncidentSignals(
        List<Deployment> deployments,
        List<MetricSample> metrics,
        List<LogEvent> logs,
        List<DependencyStatus> dependencies
) {
}

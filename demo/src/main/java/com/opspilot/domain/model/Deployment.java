package com.opspilot.domain.model;

import com.opspilot.domain.enums.DeploymentStatus;

import java.time.Instant;
import java.util.List;

public record Deployment(
        String deploymentId,
        String service,
        String version,
        Instant deployedAt,
        String deployedBy,
        String commitSha,
        DeploymentStatus status,
        List<String> changes
) {
}

package com.opspilot.interfaces.rest.dto;

import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.domain.enums.Severity;
import com.opspilot.domain.model.Incident;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 故障查询响应。
 *
 * <p>把领域对象转换成稳定的 API 契约，避免以后领域字段调整直接影响外部调用方。
 */
public record IncidentResponse(
        String incidentId,
        String alertId,
        String title,
        String service,
        Severity severity,
        IncidentStatus status,
        Instant startedAt,
        Instant resolvedAt,
        List<String> affectedServices,
        Map<String, String> metadata
) {

    public static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.incidentId(),
                incident.alertId(),
                incident.title(),
                incident.service(),
                incident.severity(),
                incident.status(),
                incident.startedAt(),
                incident.resolvedAt(),
                incident.affectedServices(),
                incident.metadata()
        );
    }
}

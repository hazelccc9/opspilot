package com.opspilot.interfaces.rest.dto;

import com.opspilot.domain.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * 创建故障的告警请求。
 *
 * <p>只暴露接入告警所需的字段；服务端会补齐 incidentId、status、affectedServices 等。
 */
public record CreateAlertRequest(
        @NotBlank String alertId,
        @NotBlank String service,
        @NotBlank String symptom,
        @NotNull Severity severity,
        @NotNull Instant firedAt,
        @NotBlank String source,
        @NotBlank String description,
        Map<String, String> labels
) {
}

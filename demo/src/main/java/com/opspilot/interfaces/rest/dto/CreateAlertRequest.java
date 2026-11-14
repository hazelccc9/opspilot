package com.opspilot.interfaces.rest.dto;

import com.opspilot.domain.enums.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Map;

/**
 * 创建故障的告警请求
 *
 * 只暴露接入告警所需的字段；服务端会补齐 incidentId、status、affectedServices 等。
 */
public record CreateAlertRequest(
        /* 告警唯一 ID，必填 */
        @NotBlank String alertId,
        /* 触发告警的服务名，必填 */
        @NotBlank String service,
        /* 告警症状摘要，必填 */
        @NotBlank String symptom,
        /* 严重级别，必填 */
        @NotNull Severity severity,
        /* 告警触发时间，必填 */
        @NotNull Instant firedAt,
        /* 告警来源，必填 */
        @NotBlank String source,
        /* 人类可读的告警详情，必填 */
        @NotBlank String description,
        /* 告警维度标签；可为空，服务端会转换为空 Map */
        Map<String, String> labels
) {
}

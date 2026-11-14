package com.opspilot.interfaces.rest.dto;

import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.domain.enums.Severity;
import com.opspilot.domain.model.Incident;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 故障查询响应
 *
 * 把领域对象转换成稳定的 API 契约，避免以后领域字段调整直接影响外部调用方。
 */
public record IncidentResponse(
        /* 故障唯一 ID */
        String incidentId,
        /* 触发该故障的告警 ID */
        String alertId,
        /* 故障标题 */
        String title,
        /* 主服务名 */
        String service,
        /* 故障严重级别 */
        Severity severity,
        /* 当前处理状态 */
        IncidentStatus status,
        /* 故障开始时间（UTC） */
        Instant startedAt,
        /* 恢复时间（UTC）；未恢复时为 null */
        Instant resolvedAt,
        /* 受影响的服务列表 */
        List<String> affectedServices,
        /* 扩展元数据 */
        Map<String, String> metadata
) {

    /**
     * 把领域对象转换为 API 响应。
     *
     * @param incident 故障领域对象
     * @return 对外响应 DTO
     */
    public static IncidentResponse from(Incident incident) {
        // 逐字段映射，保持 API 契约与领域模型解耦
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

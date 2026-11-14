package com.opspilot.domain.model;

import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.domain.enums.Severity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 故障事件，是 Agent 调查和处置的运行时聚合根。
 *
 * 如果说 {@link Alert} 是“监控系统喊了一声”，那么 {@code Incident} 就是
 * “我们决定把这件事当成一次故障来处理”。它把告警、状态、受影响范围和审计元数据
 * 组织在一起，后续的 {@link Evidence}、{@link Hypothesis}、{@link Action} 都挂在它下面。
 */
public record Incident(
        /* 故障唯一 ID */
        String incidentId,
        /* 触发该故障的告警 ID，对应 Alert.alertId */
        String alertId,
        /* 故障标题，便于工单和看板展示 */
        String title,
        /* 主服务名；多服务故障时取影响最大的服务 */
        String service,
        /* 故障级别，通常继承自告警，也可以由人工调整 */
        Severity severity,
        /* 当前处理状态，见 IncidentStatus */
        IncidentStatus status,
        /* 故障开始时间（UTC） */
        Instant startedAt,
        /* 恢复时间（UTC）；未恢复或未知时为 null */
        Instant resolvedAt,
        /* 受影响的服务列表；可能为空，表示尚未确认影响范围 */
        List<String> affectedServices,
        /* 扩展元数据，例如团队、环境、集群、外部工单号；可能为空 Map，但不应为 null */
        Map<String, String> metadata
) {
}

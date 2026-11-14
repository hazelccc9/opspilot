package com.opspilot.application.incident;

import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.domain.model.Alert;
import com.opspilot.domain.model.Incident;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 故障事件的应用服务：告警接入、故障创建、故障查询。
 *
 * <p>这里刻意不依赖 Web、数据库或 LLM，方便后续被 REST、CLI、Agent 和评测复用。
 */
@Service
public class IncidentService {

    private final IncidentRepository repository;

    public IncidentService(IncidentRepository repository) {
        this.repository = repository;
    }

    /**
     * 由一条告警创建故障事件。
     *
     * <p>Day 3 只做“接入 + 建单”，真正的调查由后续 Agent 运行时触发。
     * 同一 alertId 重复创建会抛 {@link DuplicateAlertException}。
     */
    public Incident createIncident(Alert alert) {
        Objects.requireNonNull(alert, "alert must not be null");

        Incident incident = new Incident(
                "INC-" + UUID.randomUUID(),
                alert.alertId(),
                alert.service() + ": " + alert.symptom(),
                alert.service(),
                alert.severity(),
                IncidentStatus.NEW,
                alert.firedAt(),
                null,
                List.of(alert.service()),
                Map.of(
                        "source", alert.source(),
                        "alertDescription", alert.description()
                )
        );

        return repository.save(incident);
    }

    /** 按 ID 查询故障，不存在时抛 {@link IncidentNotFoundException}。 */
    public Incident getIncident(String incidentId) {
        return repository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));
    }

    /**
     * 查询故障列表，支持按状态和服务过滤。
     *
     * @param status  可选状态；为 null 时不过滤
     * @param service 可选服务名；为 null 或空字符串时不过滤
     */
    public List<Incident> listIncidents(IncidentStatus status, String service) {
        return repository.findAll().stream()
                .filter(incident -> status == null || incident.status() == status)
                .filter(incident -> service == null
                        || service.isBlank()
                        || incident.service().equalsIgnoreCase(service))
                .sorted(Comparator.comparing(Incident::startedAt).reversed())
                .toList();
    }
}

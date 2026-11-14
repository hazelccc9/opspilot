package com.opspilot.infrastructure.persistence;

import com.opspilot.application.incident.DuplicateAlertException;
import com.opspilot.application.incident.IncidentRepository;
import com.opspilot.domain.model.Incident;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Day 3 的内存版故障仓库。
 *
 * <p>它让业务骨架先跑起来，避免在领域模型还没稳定时过早设计数据库表。
 * 重启后数据会丢失；等 Day 4/5 需要持久化证据、动作和审计时，再替换为 H2/PostgreSQL 实现。
 */
@Repository
public class InMemoryIncidentRepository implements IncidentRepository {

    private final Map<String, Incident> incidentsById = new ConcurrentHashMap<>();
    private final Map<String, String> incidentIdByAlertId = new ConcurrentHashMap<>();

    @Override
    public Incident save(Incident incident) {
        String existingIncidentId = incidentIdByAlertId.putIfAbsent(
                incident.alertId(), incident.incidentId());

        if (existingIncidentId != null) {
            throw new DuplicateAlertException(incident.alertId());
        }

        incidentsById.put(incident.incidentId(), incident);
        return incident;
    }

    @Override
    public Optional<Incident> findById(String incidentId) {
        return Optional.ofNullable(incidentsById.get(incidentId));
    }

    @Override
    public Optional<Incident> findByAlertId(String alertId) {
        String incidentId = incidentIdByAlertId.get(alertId);
        return incidentId == null ? Optional.empty() : findById(incidentId);
    }

    @Override
    public List<Incident> findAll() {
        return List.copyOf(incidentsById.values());
    }
}

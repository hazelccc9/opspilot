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
 * 它让业务骨架先跑起来，避免在领域模型还没稳定时过早设计数据库表。
 * 重启后数据会丢失；等 Day 4/5 需要持久化证据、动作和审计时，再替换为 H2/PostgreSQL 实现。
 */
@Repository
public class InMemoryIncidentRepository implements IncidentRepository {

    /** 以故障 ID 为键的故障存储，使用并发 Map 支撑 Web 层并发访问。 */
    private final Map<String, Incident> incidentsById = new ConcurrentHashMap<>();

    /** 告警 ID 到故障 ID 的映射，用于保证同一告警只建一次故障。 */
    private final Map<String, String> incidentIdByAlertId = new ConcurrentHashMap<>();

    /**
     * 保存故障事件。
     *
     * @param incident 待保存的故障事件
     * @return 保存后的故障事件
     * @throws DuplicateAlertException 同一个 alertId 已经创建过故障
     */
    @Override
    public Incident save(Incident incident) {
        // 先原子占位 alertId；putIfAbsent 保证并发下也只有一个请求能成功
        String existingIncidentId = incidentIdByAlertId.putIfAbsent(
                incident.alertId(), incident.incidentId());

        if (existingIncidentId != null) {
            // 该告警已经关联过故障，拒绝重复建单
            throw new DuplicateAlertException(incident.alertId());
        }

        // 告警索引建立成功后再写入主存储
        incidentsById.put(incident.incidentId(), incident);
        return incident;
    }

    /**
     * 按故障 ID 查询。
     *
     * @param incidentId 故障 ID
     * @return 匹配的故障；不存在时返回 empty
     */
    @Override
    public Optional<Incident> findById(String incidentId) {
        // Map 可能返回 null，这里统一包装成 Optional
        return Optional.ofNullable(incidentsById.get(incidentId));
    }

    /**
     * 按告警 ID 查询。
     *
     * @param alertId 告警 ID
     * @return 匹配的故障；不存在时返回 empty
     */
    @Override
    public Optional<Incident> findByAlertId(String alertId) {
        // 先通过告警索引拿到故障 ID
        String incidentId = incidentIdByAlertId.get(alertId);
        // 没有索引记录时直接返回 empty，否则复用 findById 查询详情
        return incidentId == null ? Optional.empty() : findById(incidentId);
    }

    /**
     * 返回当前所有故障。
     *
     * @return 故障列表快照
     */
    @Override
    public List<Incident> findAll() {
        // 复制一份不可变列表，避免调用方修改仓库内部集合
        return List.copyOf(incidentsById.values());
    }
}

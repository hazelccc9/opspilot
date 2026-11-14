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
 * 故障事件的应用服务
 */
@Service
public class IncidentService {

    /**
     * 故障事件的持久化端口
     */
    private final IncidentRepository repository;

    /**
     * 构造故障应用服务
     *
     * @param repository 故障事件的持久化端口
     */
    public IncidentService(IncidentRepository repository) {
        // 注入仓库端口，应用服务只依赖接口而不关心具体存储实现
        this.repository = repository;
    }

    /**
     * 创建故障事件
     *
     * Day 3 只做“接入 + 建单”，真正的调查由后续 Agent 运行时触发。
     * 同一 alertId 重复创建会抛 {@link DuplicateAlertException}。
     *
     * @param alert 原始告警
     * @return 新建的故障事件
     */
    public Incident createIncident(Alert alert) {
        // 告警是建单的必需输入，先做显式校验，避免后续出现难排查的空指针
        Objects.requireNonNull(alert, "alert must not be null");

        // 把告警快照转换为故障聚合：状态从 NEW 开始，影响范围暂定为主服务
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

        // 交给仓库持久化，并由仓库保证 alertId 的幂等性
        return repository.save(incident);
    }

    /**
     * 按 ID 查询故障，不存在时抛 {@link IncidentNotFoundException}。
     *
     * @param incidentId 故障 ID
     * @return 匹配的故障事件
     */
    public Incident getIncident(String incidentId) {
        // 仓库返回 Optional，这里统一转换为业务异常，避免 Controller 处理空的细节
        return repository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(incidentId));
    }

    /**
     * 查询故障列表，支持按状态和服务过滤。
     *
     * @param status  可选状态；为 null 时不过滤
     * @param service 可选服务名；为 null 或空字符串时不过滤
     * @return 按开始时间倒序排列的故障列表
     */
    public List<Incident> listIncidents(IncidentStatus status, String service) {
        return repository.findAll().stream()
                // 状态为空表示不限制；否则只保留状态完全匹配的故障
                .filter(incident -> status == null || incident.status() == status)
                // 服务名为空表示不限制；比较时忽略大小写，降低调用方参数格式负担
                .filter(incident -> service == null
                        || service.isBlank()
                        || incident.service().equalsIgnoreCase(service))
                // 最新的故障排在前面，便于看板和列表页展示
                .sorted(Comparator.comparing(Incident::startedAt).reversed())
                .toList();
    }
}

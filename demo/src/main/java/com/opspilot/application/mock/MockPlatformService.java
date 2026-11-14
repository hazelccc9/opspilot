package com.opspilot.application.mock;

import com.opspilot.domain.enums.LogLevel;
import com.opspilot.domain.model.Alert;
import com.opspilot.domain.model.DependencyStatus;
import com.opspilot.domain.model.Deployment;
import com.opspilot.domain.model.IncidentCase;
import com.opspilot.domain.model.LogEvent;
import com.opspilot.domain.model.MetricSample;
import com.opspilot.infrastructure.dataset.JsonIncidentDatasetLoader;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Mock 平台查询服务：把 50 条合成故障集当成一个“只读监控环境”来查询。
 *
 * <p>它模拟外部监控/日志/发布/CMDB 系统，供后续 Agent 工具调用。
 * 所有查询只返回原始信号，不返回 {@code groundTruth}。
 */
@Service
public class MockPlatformService {

    private static final int MAX_LOG_LIMIT = 500;

    private final List<IncidentCase> cases;
    private final Map<String, IncidentCase> casesByCaseId;
    private final Map<String, IncidentCase> casesByAlertId;
    private volatile IncidentCase activeCase;

    public MockPlatformService(JsonIncidentDatasetLoader loader) {
        this.cases = loader.load();
        this.casesByCaseId = index(this.cases, IncidentCase::caseId);
        this.casesByAlertId = index(this.cases, incident -> incident.alert().alertId());
    }

    /** 返回可安全展示的用例摘要，不含 ground truth。 */
    public List<MockCaseSummary> listCases() {
        return cases.stream()
                .map(incident -> new MockCaseSummary(
                        incident.caseId(),
                        incident.service(),
                        incident.alert().alertId(),
                        incident.alert().symptom()))
                .toList();
    }

    /** 按告警 ID 查询告警。 */
    public Alert getAlert(String alertId) {
        IncidentCase incident = casesByAlertId.get(alertId);
        if (incident == null) {
            throw new MockResourceNotFoundException("Alert not found: " + alertId);
        }
        return incident.alert();
    }

    /** 查询指标采样点，按时间升序返回。 */
    public List<MetricSample> queryMetrics(String service, String name, Instant from, Instant to) {
        validateRange(from, to);
        return cases.stream()
                .flatMap(incident -> incident.signals().metrics().stream())
                .filter(metric -> metric.service().equalsIgnoreCase(service))
                .filter(metric -> name == null || name.isBlank() || metric.name().equals(name))
                .filter(metric -> within(metric.timestamp(), from, to))
                .sorted(Comparator.comparing(MetricSample::timestamp))
                .toList();
    }

    /**
     * 查询日志，按时间倒序返回最近的日志。
     *
     * @param query 可选关键字，匹配 message 或 attributes 的值
     * @param limit 返回条数，默认 100，最大 500
     */
    public List<LogEvent> queryLogs(String service, String query, Instant from, Instant to,
                                    LogLevel level, int limit) {
        validateRange(from, to);
        String keyword = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
        int safeLimit = Math.max(1, Math.min(limit, MAX_LOG_LIMIT));

        return cases.stream()
                .flatMap(incident -> incident.signals().logs().stream())
                .filter(log -> log.service().equalsIgnoreCase(service))
                .filter(log -> level == null || log.level() == level)
                .filter(log -> within(log.timestamp(), from, to))
                .filter(log -> matches(log, keyword))
                .sorted(Comparator.comparing(LogEvent::timestamp).reversed())
                .limit(safeLimit)
                .toList();
    }

    /** 查询发布/配置变更记录，按时间倒序返回。 */
    public List<Deployment> queryDeployments(String service, Instant from, Instant to) {
        validateRange(from, to);
        return cases.stream()
                .flatMap(incident -> incident.signals().deployments().stream())
                .filter(deployment -> deployment.service().equalsIgnoreCase(service))
                .filter(deployment -> within(deployment.deployedAt(), from, to))
                .sorted(Comparator.comparing(Deployment::deployedAt).reversed())
                .toList();
    }

    /** 查询依赖健康度快照。 */
    public List<DependencyStatus> queryDependencies(String service, String dependsOn) {
        return cases.stream()
                .flatMap(incident -> incident.signals().dependencies().stream())
                .filter(dependency -> dependency.service().equalsIgnoreCase(service))
                .filter(dependency -> dependsOn == null
                        || dependsOn.isBlank()
                        || dependency.dependsOn().equalsIgnoreCase(dependsOn))
                .toList();
    }

    /** 激活一条故障用例，模拟“故障注入后产生告警”。 */
    public MockFaultActivation activate(String caseId) {
        IncidentCase incident = casesByCaseId.get(caseId);
        if (incident == null) {
            throw new MockResourceNotFoundException("Mock case not found: " + caseId);
        }
        activeCase = incident;
        return new MockFaultActivation(incident.caseId(), incident.alert());
    }

    /** 返回当前激活的故障；没有激活时为 empty。 */
    public Optional<MockFaultActivation> activeCase() {
        IncidentCase incident = activeCase;
        return incident == null
                ? Optional.empty()
                : Optional.of(new MockFaultActivation(incident.caseId(), incident.alert()));
    }

    /** 清除当前激活的故障。 */
    public void reset() {
        activeCase = null;
    }

    private static boolean matches(LogEvent log, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (log.message().toLowerCase(Locale.ROOT).contains(keyword)) {
            return true;
        }
        return log.attributes().values().stream()
                .anyMatch(value -> value != null && value.toLowerCase(Locale.ROOT).contains(keyword));
    }

    private static boolean within(Instant timestamp, Instant from, Instant to) {
        return (from == null || !timestamp.isBefore(from))
                && (to == null || !timestamp.isAfter(to));
    }

    private static void validateRange(Instant from, Instant to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
    }

    private static Map<String, IncidentCase> index(
            List<IncidentCase> cases, Function<IncidentCase, String> keyExtractor) {
        Map<String, IncidentCase> index = new HashMap<>();
        for (IncidentCase incident : cases) {
            String key = keyExtractor.apply(incident);
            if (index.putIfAbsent(key, incident) != null) {
                throw new IllegalStateException("Duplicate mock case key: " + key);
            }
        }
        return Map.copyOf(index);
    }
}

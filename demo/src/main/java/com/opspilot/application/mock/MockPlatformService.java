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
 * Mock 平台查询服务，把 50 条合成故障集当成一个“只读监控环境”来查询。
 *
 * 它模拟外部监控/日志/发布/CMDB 系统，供后续 Agent 工具调用。
 * 所有查询只返回原始信号，不返回 {@code groundTruth}。
 */
@Service
public class MockPlatformService {

    /** 单次日志查询允许返回的最大条数，避免一次拉取过多数据。 */
    private static final int MAX_LOG_LIMIT = 500;

    /** 启动时加载的完整故障用例列表，作为所有 Mock 查询的数据源。 */
    private final List<IncidentCase> cases;

    /** 按 caseId 建立的用例索引，支持 O(1) 激活和查询。 */
    private final Map<String, IncidentCase> casesByCaseId;

    /** 按 alertId 建立的用例索引，支持 O(1) 查询告警。 */
    private final Map<String, IncidentCase> casesByAlertId;

    /** 当前激活的故障用例；未激活时为 null，使用 volatile 保证多线程可见性。 */
    private volatile IncidentCase activeCase;

    /**
     * 构造 Mock 平台查询服务。
     *
     * @param loader 合成故障数据集加载器，启动时一次性加载全部用例并建立 caseId / alertId 索引
     */
    public MockPlatformService(JsonIncidentDatasetLoader loader) {
        // 启动时一次性加载数据集，避免每次查询都读文件
        this.cases = loader.load();
        // 分别建立 caseId 和 alertId 索引，供按 ID 的精确查询使用
        this.casesByCaseId = index(this.cases, IncidentCase::caseId);
        this.casesByAlertId = index(this.cases, incident -> incident.alert().alertId());
    }

    /**
     * 返回可安全展示的用例摘要，不含 ground truth。
     *
     * @return 用例摘要列表
     */
    public List<MockCaseSummary> listCases() {
        // 只挑选可公开字段，避免把 groundTruth 透出给调用方
        return cases.stream()
                .map(incident -> new MockCaseSummary(
                        incident.caseId(),
                        incident.service(),
                        incident.alert().alertId(),
                        incident.alert().symptom()))
                .toList();
    }

    /**
     * 按告警 ID 查询告警。
     *
     * @param alertId 告警 ID
     * @return 对应的告警快照
     * @throws MockResourceNotFoundException 告警 ID 不存在
     */
    public Alert getAlert(String alertId) {
        // 通过 alertId 索引直接定位用例
        IncidentCase incident = casesByAlertId.get(alertId);
        if (incident == null) {
            // 找不到时抛出业务异常，由 API 层转换为 404
            throw new MockResourceNotFoundException("Alert not found: " + alertId);
        }
        // 只返回告警本身，不暴露用例里的其他字段
        return incident.alert();
    }

    /**
     * 查询指标采样点，按时间升序返回。
     *
     * @param service 服务名，必填
     * @param name    可选指标名；为空时返回该服务的全部指标
     * @param from    可选起始时间（包含）
     * @param to      可选结束时间（包含）
     * @return 指标采样点列表
     */
    public List<MetricSample> queryMetrics(String service, String name, Instant from, Instant to) {
        // 先校验时间窗，避免非法查询悄悄返回空结果
        validateRange(from, to);
        return cases.stream()
                // 把每个用例的指标列表摊平，形成全局指标流
                .flatMap(incident -> incident.signals().metrics().stream())
                // 服务名忽略大小写匹配
                .filter(metric -> metric.service().equalsIgnoreCase(service))
                // 指标名为空表示不过滤，否则精确匹配
                .filter(metric -> name == null || name.isBlank() || metric.name().equals(name))
                // 只保留落在查询时间窗内的采样点
                .filter(metric -> within(metric.timestamp(), from, to))
                // 指标趋势通常按时间正序阅读
                .sorted(Comparator.comparing(MetricSample::timestamp))
                .toList();
    }

    /**
     * 查询日志，按时间倒序返回最近的日志。
     *
     * @param service 服务名，必填
     * @param query   可选关键字，匹配 message 或 attributes 的值
     * @param from    可选起始时间（包含）
     * @param to      可选结束时间（包含）
     * @param level   可选日志级别
     * @param limit   返回条数，默认 100，最大 500
     * @return 日志事件列表
     */
    public List<LogEvent> queryLogs(String service, String query, Instant from, Instant to,
                                    LogLevel level, int limit) {
        // 先校验时间窗，避免非法查询悄悄返回空结果
        validateRange(from, to);
        // 关键字统一 trim + 小写，后续匹配可以忽略大小写
        String keyword = query == null ? null : query.trim().toLowerCase(Locale.ROOT);
        // 限制返回条数的上下界，防止 limit 为负数或过大
        int safeLimit = Math.max(1, Math.min(limit, MAX_LOG_LIMIT));

        return cases.stream()
                // 把每个用例的日志列表摊平，形成全局日志流
                .flatMap(incident -> incident.signals().logs().stream())
                // 服务名忽略大小写匹配
                .filter(log -> log.service().equalsIgnoreCase(service))
                // 日志级别为空表示不过滤
                .filter(log -> level == null || log.level() == level)
                // 只保留落在查询时间窗内的日志
                .filter(log -> within(log.timestamp(), from, to))
                // 关键字匹配 message 或结构化属性
                .filter(log -> matches(log, keyword))
                // 最近的日志优先返回
                .sorted(Comparator.comparing(LogEvent::timestamp).reversed())
                .limit(safeLimit)
                .toList();
    }

    /**
     * 查询发布/配置变更记录，按时间倒序返回。
     *
     * @param service 服务名，必填
     * @param from    可选起始时间（包含）
     * @param to      可选结束时间（包含）
     * @return 发布记录列表
     */
    public List<Deployment> queryDeployments(String service, Instant from, Instant to) {
        // 先校验时间窗，避免非法查询悄悄返回空结果
        validateRange(from, to);
        return cases.stream()
                // 把每个用例的发布记录摊平，形成全局变更时间线
                .flatMap(incident -> incident.signals().deployments().stream())
                // 服务名忽略大小写匹配
                .filter(deployment -> deployment.service().equalsIgnoreCase(service))
                // 只保留落在查询时间窗内的发布记录
                .filter(deployment -> within(deployment.deployedAt(), from, to))
                // 最近的变更优先返回，便于关联故障时间点
                .sorted(Comparator.comparing(Deployment::deployedAt).reversed())
                .toList();
    }

    /**
     * 查询依赖健康度快照。
     *
     * @param service   调用方服务名，必填
     * @param dependsOn 可选被依赖服务名；为空时返回该调用方的全部依赖
     * @return 依赖健康度列表
     */
    public List<DependencyStatus> queryDependencies(String service, String dependsOn) {
        return cases.stream()
                // 把每个用例的依赖快照摊平，形成全局依赖视图
                .flatMap(incident -> incident.signals().dependencies().stream())
                // 先按调用方服务过滤
                .filter(dependency -> dependency.service().equalsIgnoreCase(service))
                // 被依赖服务为空表示不过滤，否则忽略大小写匹配
                .filter(dependency -> dependsOn == null
                        || dependsOn.isBlank()
                        || dependency.dependsOn().equalsIgnoreCase(dependsOn))
                .toList();
    }

    /**
     * 激活一条故障用例，模拟“故障注入后产生告警”。
     *
     * @param caseId 用例 ID
     * @return 激活结果，包含用例 ID 和对应告警
     * @throws MockResourceNotFoundException 用例 ID 不存在
     */
    public MockFaultActivation activate(String caseId) {
        // 通过 caseId 索引定位用例
        IncidentCase incident = casesByCaseId.get(caseId);
        if (incident == null) {
            // 找不到时抛出业务异常，由 API 层转换为 404
            throw new MockResourceNotFoundException("Mock case not found: " + caseId);
        }
        // 记录当前激活用例，供 activeCase() 和 reset() 使用
        activeCase = incident;
        // 只返回用例 ID 和告警，避免泄漏标准答案
        return new MockFaultActivation(incident.caseId(), incident.alert());
    }

    /**
     * 返回当前激活的故障；没有激活时为 empty。
     *
     * @return 当前激活结果
     */
    public Optional<MockFaultActivation> activeCase() {
        // 先读入局部变量，避免并发 reset 导致空指针
        IncidentCase incident = activeCase;
        return incident == null
                ? Optional.empty()
                : Optional.of(new MockFaultActivation(incident.caseId(), incident.alert()));
    }

    /**
     * 清除当前激活的故障。
     */
    public void reset() {
        // 置空后 activeCase() 会返回 empty，接口层据此返回 204
        activeCase = null;
    }

    /**
     * 判断一条日志是否命中关键字：匹配 message 或任一 attributes 的值，忽略大小写。
     *
     * @param log     待匹配的日志
     * @param keyword 已 trim 并转小写的关键字；为 null 或空白表示不过滤
     * @return 是否命中
     */
    private static boolean matches(LogEvent log, String keyword) {
        // 没有关键字视为全部命中，保持查询语义简单
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        // 先匹配人类可读的日志正文
        if (log.message().toLowerCase(Locale.ROOT).contains(keyword)) {
            return true;
        }
        // 结构化字段里也可能藏着错误码、traceId 等线索，一并参与匹配
        return log.attributes().values().stream()
                .anyMatch(value -> value != null && value.toLowerCase(Locale.ROOT).contains(keyword));
    }

    /**
     * 判断时间戳是否落在 [from, to] 闭区间内；任一端为 null 表示该侧不设限。
     *
     * @param timestamp 待判断的时间戳
     * @param from      起始时间，可为 null
     * @param to        结束时间，可为 null
     * @return 是否在时间窗内
     */
    private static boolean within(Instant timestamp, Instant from, Instant to) {
        // 用“不早于 from 且不晚于 to”表达闭区间，天然兼容 null 边界
        return (from == null || !timestamp.isBefore(from))
                && (to == null || !timestamp.isAfter(to));
    }

    /**
     * 校验查询时间窗，from 晚于 to 时直接拒绝，避免返回空结果掩盖调用方错误。
     *
     * @param from 起始时间，可为 null
     * @param to   结束时间，可为 null
     * @throws IllegalArgumentException 当 from 晚于 to
     */
    private static void validateRange(Instant from, Instant to) {
        // 只有两端都提供时才比较；单边或双边为空都视为合法
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
    }

    /**
     * 按指定 key 把用例建成不可变索引，供 O(1) 查询使用。
     *
     * @param cases        故障用例列表
     * @param keyExtractor 从用例中提取索引键
     * @return 不可变索引
     * @throws IllegalStateException 数据集里出现重复 key
     */
    private static Map<String, IncidentCase> index(
            List<IncidentCase> cases, Function<IncidentCase, String> keyExtractor) {
        Map<String, IncidentCase> index = new HashMap<>();
        for (IncidentCase incident : cases) {
            String key = keyExtractor.apply(incident);
            // 重复 key 会让按 ID 查询的结果不确定，必须在启动时就暴露
            if (index.putIfAbsent(key, incident) != null) {
                throw new IllegalStateException("Duplicate mock case key: " + key);
            }
        }
        // 包装成不可变视图，防止调用方意外修改索引
        return Map.copyOf(index);
    }
}

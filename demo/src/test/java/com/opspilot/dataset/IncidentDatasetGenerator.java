package com.opspilot.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 合成故障数据集生成器。
 *
 * 运行方式：./mvnw -Dtest=IncidentDatasetGeneratorTest -Dopspilot.generateDataset=true test
 */
public final class IncidentDatasetGenerator {

    /** 用于把用例 Map 序列化成 JSONL 单行文本的 ObjectMapper。 */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 数据集统一的时间基准点，各类故障在此基础上偏移。 */
    private static final Instant BASE = Instant.parse("2026-11-10T08:00:00Z");

    /**
     * 工具类构造函数，禁止外部实例化。
     */
    private IncidentDatasetGenerator() {
        // 工具类只提供静态方法，禁止实例化
    }

    /**
     * 数据集生成器命令行入口。
     *
     * @param args 可选输出路径；未提供时写入默认的 demo-data/incidents.jsonl
     * @throws Exception 生成或写入文件失败
     */
    public static void main(String[] args) throws Exception {
        // 优先使用命令行传入的输出路径，否则回退到仓库默认数据目录
        Path output = args.length > 0
                ? Path.of(args[0])
                : Path.of("..", "demo-data", "incidents.jsonl").normalize();
        generate(output);
        // 输出最终绝对路径，方便确认文件写到了哪里
        System.out.println("Wrote 50 incidents to " + output.toAbsolutePath());
    }

    /**
     * 生成 50 条故障用例并写入指定文件。
     *
     * @param output 目标 JSONL 文件路径
     * @throws Exception 创建目录或写入文件失败
     */
    public static void generate(Path output) throws Exception {
        // 预留 50 条用例容量，避免列表反复扩容
        List<Map<String, Object>> cases = new ArrayList<>(50);
        int caseNo = 1;
        // 每类根因生成 10 条，依次消耗用例编号
        caseNo = addBadDeploy(cases, caseNo);
        caseNo = addDependencyTimeout(cases, caseNo);
        caseNo = addConnectionPoolExhaustion(cases, caseNo);
        caseNo = addTrafficSpike(cases, caseNo);
        addConfigError(cases, caseNo);

        // 确保输出目录存在，再以 UTF-8 逐行写入 JSONL
        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (Map<String, Object> incident : cases) {
                // 每条用例序列化成一行，行之间用换行符分隔
                writer.write(MAPPER.writeValueAsString(incident));
                writer.write('\n');
            }
        }
    }

    /**
     * 追加 10 条坏发布（BAD_DEPLOY）用例。
     *
     * @param cases 用例列表，方法会把新用例追加进去
     * @param no    当前起始用例编号
     * @return 下一批用例的起始编号
     */
    private static int addBadDeploy(List<Map<String, Object>> cases, int no) {
        // 每条用例轮换使用不同服务，扩大数据集覆盖面
        String[] services = {
                "order-service", "payment-service", "user-service", "cart-service", "search-service",
                "notification-service", "inventory-service", "api-gateway", "coupon-service", "review-service"
        };
        for (int i = 0; i < 10; i++) {
            // 用例编号从传入的 no 开始递增
            int caseNo = no + i;
            String service = services[i];
            // 每条用例间隔 1 小时，保证时间线清晰
            Instant firedAt = BASE.plusSeconds(i * 3600L);
            // 发布发生在告警前 15 分钟，用于建立“变更 -> 故障”的时间关联
            Instant deployedAt = firedAt.minusSeconds(15 * 60L);
            String version = "v1.8." + i;

            // 坏发布信号：一条近期发布记录
            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, version, deployedAt,
                            "refactor payment callback", "upgrade http client")
            );
            // 错误率、P95 延迟和 CPU 均随用例递增，模拟不同程度的回归
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.12 + i * 0.01, "ratio",
                            map("status", "5xx")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 1800 + i * 120, "ms",
                            map("status", "all")),
                    metric("jvm.cpu.usage", service, firedAt.minusSeconds(60), 0.55 + i * 0.01, "ratio", map())
            );
            // 两条错误日志分别对应业务异常和下游回调超时
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "unexpected exception in request handler", map("endpoint", "/api/orders")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "payment callback timeout after 3000ms", map("paymentId", "pay_" + caseNo))
            );
            // 依赖本身健康，用于排除依赖故障这一可能根因
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "payment-service", "UP", 120 + i * 5, 0.01)
            );

            // 第 8、9 条用例提高风险并要求升级人工
            boolean escalate = i >= 8;
            Map<String, Object> groundTruth = groundTruth(
                    "BAD_DEPLOY",
                    service + " " + version + " introduced a regression; rollback to the previous version",
                    List.of("DEPLOYMENT", "METRIC", "LOG"),
                    "ROLLBACK",
                    escalate ? "HIGH" : "MEDIUM",
                    escalate,
                    "Recent deployment correlates with the error and latency spike; rollback and verify."
            );

            // 组装单条用例：告警输入 + 信号包 + 标准答案
            cases.add(caseMap(caseNo, "BAD_DEPLOY", service,
                    alert(caseNo, service, "HTTP 5xx rate > 10% for 5m",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " 5xx rate " + (12 + i) + "%, p95 " + (1800 + i * 120) + "ms"),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        // 当前批次消耗 10 个编号
        return no + 10;
    }

    /**
     * 追加 10 条依赖超时（DEPENDENCY_TIMEOUT）用例。
     *
     * @param cases 用例列表，方法会把新用例追加进去
     * @param no    当前起始用例编号
     * @return 下一批用例的起始编号
     */
    private static int addDependencyTimeout(List<Map<String, Object>> cases, int no) {
        // 轮换使用不同调用方服务
        String[] services = {
                "order-service", "payment-service", "checkout-service", "cart-service", "inventory-service",
                "notification-service", "search-service", "user-service", "coupon-service", "review-service"
        };
        for (int i = 0; i < 10; i++) {
            // 用例编号从传入的 no 开始递增
            int caseNo = no + i;
            String service = services[i];
            // 第二天开始，每条用例间隔 1 小时
            Instant firedAt = BASE.plusSeconds(24 * 3600L + i * 3600L);
            // 发布发生在 3 天前，用于弱化“坏发布”假设
            Instant deployedAt = firedAt.minusSeconds(3 * 24 * 3600L);

            // 一条常规发布记录，时间上远离故障
            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.7." + i, deployedAt,
                            "routine dependency bump", "logging cleanup")
            );
            // 下游调用延迟、调用方错误率和整体 P95 均升高
            List<Map<String, Object>> metrics = List.of(
                    metric("http.client.requests.p95", service, firedAt.minusSeconds(60), 2500 + i * 100, "ms",
                            map("dependency", "payment-service")),
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.08 + i * 0.005, "ratio",
                            map("status", "5xx")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 2200 + i * 80, "ms",
                            map("status", "all"))
            );
            // 日志中同时出现读取超时和熔断打开
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "Read timed out after 3000ms calling payment-service",
                            map("dependency", "payment-service")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "WARN",
                            "circuit breaker open for payment-service", map("dependency", "payment-service"))
            );
            // 依赖快照显示 payment-service 已降级
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "payment-service", "DEGRADED", 2800 + i * 50, 0.12 + i * 0.01)
            );

            Map<String, Object> groundTruth = groundTruth(
                    "DEPENDENCY_TIMEOUT",
                    "payment-service is timing out; " + service + " is blocked on the downstream call",
                    List.of("METRIC", "LOG", "DEPENDENCY"),
                    "ENABLE_CIRCUIT_BREAKER",
                    "MEDIUM",
                    true,
                    "Mitigate with a circuit breaker and escalate to the payment-service owner."
            );

            // 组装单条用例：告警输入 + 信号包 + 标准答案
            cases.add(caseMap(caseNo, "DEPENDENCY_TIMEOUT", service,
                    alert(caseNo, service, "p95 latency > 3s for 5m",
                            i < 5 ? "P1" : "P2", firedAt,
                            service + " p95 " + (2500 + i * 100) + "ms, downstream payment-service degraded"),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        // 当前批次消耗 10 个编号
        return no + 10;
    }

    /**
     * 追加 10 条连接池耗尽（CONNECTION_POOL_EXHAUSTION）用例。
     *
     * @param cases 用例列表，方法会把新用例追加进去
     * @param no    当前起始用例编号
     * @return 下一批用例的起始编号
     */
    private static int addConnectionPoolExhaustion(List<Map<String, Object>> cases, int no) {
        // 轮换使用不同服务
        String[] services = {
                "order-service", "payment-service", "user-service", "inventory-service", "coupon-service",
                "notification-service", "search-service", "cart-service", "review-service", "api-gateway"
        };
        for (int i = 0; i < 10; i++) {
            // 用例编号从传入的 no 开始递增
            int caseNo = no + i;
            String service = services[i];
            // 第三天开始，每条用例间隔 1 小时
            Instant firedAt = BASE.plusSeconds(48 * 3600L + i * 3600L);
            // 发布发生在 2 天前，避免与故障时间强相关
            Instant deployedAt = firedAt.minusSeconds(2 * 24 * 3600L);

            // 一条查询重构相关的历史发布
            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.6." + i, deployedAt,
                            "query refactor", "add new repository method")
            );
            // 活跃连接、等待连接、接口 P95 和查询 P95 共同体现连接池压力
            List<Map<String, Object>> metrics = List.of(
                    metric("db.connection.active", service, firedAt.minusSeconds(60), 20 + i, "count", map("pool", "primary")),
                    metric("db.connection.pending", service, firedAt.minusSeconds(60), 5 + i, "count", map("pool", "primary")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 3000 + i * 100, "ms", map()),
                    metric("db.query.p95", service, firedAt.minusSeconds(60), 800 + i * 50, "ms", map("db", "primary"))
            );
            // 前 7 条模拟慢查询，后 3 条追加连接泄漏提示
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "HikariPool-1 - Connection is not available, request timed out after 30000ms",
                            map("pool", "HikariPool-1")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "WARN",
                            i >= 7 ? "Connection leak detection triggered" : "Slow query detected: SELECT order by id",
                            map("threshold", "1000ms"))
            );
            // 数据库依赖已降级，但错误率仍然较低
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "primary-db", "DEGRADED", 900 + i * 50, 0.02)
            );

            // 后 3 条用例假设存在连接泄漏，期望动作为重启
            boolean leakSuspected = i >= 7;
            Map<String, Object> groundTruth = groundTruth(
                    "CONNECTION_POOL_EXHAUSTION",
                    leakSuspected
                            ? "Connection leak suspected; pool exhausted and requests are timing out"
                            : "Connection pool is exhausted by slow queries; requests are waiting for a connection",
                    List.of("METRIC", "LOG"),
                    leakSuspected ? "RESTART" : "INCREASE_POOL_SIZE",
                    leakSuspected ? "HIGH" : "MEDIUM",
                    true,
                    leakSuspected
                            ? "Restart to recover, then investigate the leak."
                            : "Increase the pool as a temporary mitigation, then fix slow queries."
            );

            // 组装单条用例：告警输入 + 信号包 + 标准答案
            cases.add(caseMap(caseNo, "CONNECTION_POOL_EXHAUSTION", service,
                    alert(caseNo, service, "DB connection pool exhausted",
                            i < 5 ? "P1" : "P2", firedAt,
                            service + " active=" + (20 + i) + ", pending=" + (5 + i)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        // 当前批次消耗 10 个编号
        return no + 10;
    }

    /**
     * 追加 10 条流量突增（TRAFFIC_SPIKE）用例。
     *
     * @param cases 用例列表，方法会把新用例追加进去
     * @param no    当前起始用例编号
     * @return 下一批用例的起始编号
     */
    private static int addTrafficSpike(List<Map<String, Object>> cases, int no) {
        // 轮换使用不同服务
        String[] services = {
                "api-gateway", "order-service", "search-service", "product-service", "cart-service",
                "payment-service", "user-service", "notification-service", "review-service", "coupon-service"
        };
        for (int i = 0; i < 10; i++) {
            // 用例编号从传入的 no 开始递增
            int caseNo = no + i;
            String service = services[i];
            // 第四天开始，每条用例间隔 1 小时
            Instant firedAt = BASE.plusSeconds(72 * 3600L + i * 3600L);
            // 发布发生在 4 天前，排除坏发布因素
            Instant deployedAt = firedAt.minusSeconds(4 * 24 * 3600L);

            // 一条与故障无关的常规发布记录
            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.5." + i, deployedAt,
                            "routine release", "dependency updates")
            );
            // QPS、CPU、P95 和错误率随用例递增，但 CPU 上限为 0.98
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.qps", service, firedAt.minusSeconds(60), 5000 + i * 800, "qps", map()),
                    metric("jvm.cpu.usage", service, firedAt.minusSeconds(60), Math.min(0.98, 0.82 + i * 0.01), "ratio", map()),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 2000 + i * 100, "ms", map()),
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.02 + i * 0.002, "ratio",
                            map("status", "5xx"))
            );
            // 日志体现限流和队列满
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "WARN",
                            "rate limit exceeded for client campaign-2026-11", map("client", "campaign")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "queue full, rejecting request", map("queue", "request-queue"))
            );
            // 数据库依赖仍然健康，说明瓶颈在流量而非下游
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "primary-db", "UP", 150 + i * 10, 0.01)
            );

            // 第 8、9 条用例提高风险并要求升级人工
            boolean escalate = i >= 8;
            Map<String, Object> groundTruth = groundTruth(
                    "TRAFFIC_SPIKE",
                    "Traffic is far above baseline; " + service + " is saturated",
                    List.of("METRIC"),
                    "SCALE_UP",
                    escalate ? "HIGH" : "MEDIUM",
                    escalate,
                    "Scale out and check the traffic source; verify downstream capacity."
            );

            // 组装单条用例：告警输入 + 信号包 + 标准答案
            cases.add(caseMap(caseNo, "TRAFFIC_SPIKE", service,
                    alert(caseNo, service, "Request rate 5x baseline",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " qps=" + (5000 + i * 800) + ", cpu=" + Math.min(0.98, 0.82 + i * 0.01)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        // 当前批次消耗 10 个编号
        return no + 10;
    }

    /**
     * 追加 10 条配置错误（CONFIG_ERROR）用例。
     *
     * @param cases 用例列表，方法会把新用例追加进去
     * @param no    当前起始用例编号
     * @return 下一批用例的起始编号
     */
    private static int addConfigError(List<Map<String, Object>> cases, int no) {
        // 轮换使用不同服务
        String[] services = {
                "payment-service", "order-service", "notification-service", "user-service", "search-service",
                "inventory-service", "coupon-service", "api-gateway", "review-service", "cart-service"
        };
        for (int i = 0; i < 10; i++) {
            // 用例编号从传入的 no 开始递增
            int caseNo = no + i;
            String service = services[i];
            // 第五天开始，每条用例间隔 1 小时
            Instant firedAt = BASE.plusSeconds(96 * 3600L + i * 3600L);
            // 配置变更发生在告警前 10 分钟
            Instant changedAt = firedAt.minusSeconds(10 * 60L);
            String version = "config-2026.11.10-" + (i + 1);

            // 配置变更同样以 Deployment 记录，版本使用 config- 前缀
            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, version, changedAt,
                            "update payment.timeout", "enable new feature flag")
            );
            // 错误率、配置刷新失败次数和 P95 共同体现配置异常
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.15 + i * 0.01, "ratio",
                            map("status", "5xx")),
                    metric("config.refresh.failures", service, firedAt.minusSeconds(60), 1 + i, "count", map()),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 1500 + i * 100, "ms", map())
            );
            // 日志包含非法配置值和配置绑定失败
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "Invalid configuration value for payment.timeout: expected duration, got '3s'",
                            map("property", "payment.timeout")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "Failed to bind configuration properties", map("property", "payment.retry.count"))
            );
            // 配置中心依赖正常，问题来自配置内容本身
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "config-service", "UP", 80 + i * 5, 0.01)
            );

            // 后 3 条用例建议直接回滚配置，其余建议修复配置值
            boolean rollback = i >= 7;
            Map<String, Object> groundTruth = groundTruth(
                    "CONFIG_ERROR",
                    "A configuration change introduced an invalid value for " + service,
                    List.of("DEPLOYMENT", "METRIC", "LOG"),
                    rollback ? "ROLLBACK" : "FIX_CONFIG",
                    rollback ? "HIGH" : "MEDIUM",
                    rollback,
                    rollback
                            ? "Rollback the config change; the invalid value is blocking startup/refresh."
                            : "Fix the configuration value and refresh."
            );

            // 组装单条用例：告警输入 + 信号包 + 标准答案
            cases.add(caseMap(caseNo, "CONFIG_ERROR", service,
                    alert(caseNo, service, "5xx rate > 15% after config change",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " config refresh failures=" + (1 + i)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        // 当前批次消耗 10 个编号
        return no + 10;
    }

    /**
     * 组装一条完整用例。
     *
     * @param caseNo      用例编号
     * @param category    根因分类
     * @param service     主服务名
     * @param alert       告警输入
     * @param signals     原始信号包
     * @param groundTruth 标准答案
     * @return 用例 Map
     */
    private static Map<String, Object> caseMap(int caseNo, String category, String service,
                                               Map<String, Object> alert,
                                               Map<String, Object> signals,
                                               Map<String, Object> groundTruth) {
        // 用例 ID 使用三位补零格式，便于排序和阅读
        return map(
                "caseId", String.format("CASE-%03d", caseNo),
                "category", category,
                "service", service,
                "alert", alert,
                "signals", signals,
                "groundTruth", groundTruth
        );
    }

    /**
     * 构造一条告警。
     *
     * @param caseNo      用例编号，同时用于生成告警 ID
     * @param service     服务名
     * @param symptom     症状摘要
     * @param severity    严重级别
     * @param firedAt     告警时间
     * @param description 详细描述
     * @return 告警 Map
     */
    private static Map<String, Object> alert(int caseNo, String service, String symptom, String severity,
                                             Instant firedAt, String description) {
        // 告警 ID 与用例编号一一对应
        return map(
                "alertId", String.format("ALERT-%03d", caseNo),
                "service", service,
                "symptom", symptom,
                "severity", severity,
                "firedAt", firedAt.toString(),
                "source", "prometheus",
                "description", description,
                "labels", map("env", "prod", "region", "cn-hangzhou", "cluster", "prod-hz-1")
        );
    }

    /**
     * 构造一条发布/配置变更记录。
     *
     * @param caseNo     用例编号，同时用于生成发布 ID
     * @param service    服务名
     * @param version    版本号
     * @param deployedAt 发布时间
     * @param changes    变更摘要
     * @return 发布记录 Map
     */
    private static Map<String, Object> deployment(int caseNo, String service, String version, Instant deployedAt,
                                                  String... changes) {
        // 发布 ID 与用例编号绑定，并生成一个可读的提交 SHA
        return map(
                "deploymentId", "DEP-" + caseNo,
                "service", service,
                "version", version,
                "deployedAt", deployedAt.toString(),
                "deployedBy", "ci-bot",
                "commitSha", String.format("%07x", 0xA1B2C3 + caseNo),
                "status", "SUCCESS",
                "changes", List.of(changes)
        );
    }

    /**
     * 构造一个指标采样点。
     *
     * @param name      指标名
     * @param service   服务名
     * @param timestamp 采样时间
     * @param value     指标值
     * @param unit      单位
     * @param labels    维度标签
     * @return 指标采样点 Map
     */
    private static Map<String, Object> metric(String name, String service, Instant timestamp, double value,
                                              String unit, Map<String, ?> labels) {
        // 逐字段写入 Map，保持与 IncidentSignals 中的结构一致
        return map(
                "name", name,
                "service", service,
                "timestamp", timestamp.toString(),
                "value", value,
                "unit", unit,
                "labels", labels
        );
    }

    /**
     * 构造一条日志事件。
     *
     * @param caseNo     用例编号，用于生成日志 ID
     * @param index      日志序号，用于区分同用例内的多条日志
     * @param service    服务名
     * @param timestamp  日志时间
     * @param level      日志级别
     * @param message    日志正文
     * @param attributes 结构化字段
     * @return 日志事件 Map
     */
    private static Map<String, Object> log(int caseNo, int index, String service, Instant timestamp,
                                           String level, String message, Map<String, ?> attributes) {
        // 日志 ID 和 traceId 都由用例编号 + 序号组成，便于串起同一请求
        return map(
                "logId", "LOG-" + caseNo + "-" + index,
                "service", service,
                "timestamp", timestamp.toString(),
                "level", level,
                "message", message,
                "traceId", "trace-" + caseNo + "-" + index,
                "attributes", attributes
        );
    }

    /**
     * 构造一条依赖健康度快照。
     *
     * @param service      调用方服务名
     * @param dependsOn    被依赖服务名
     * @param status       健康状态
     * @param latencyP95Ms P95 延迟（毫秒）
     * @param errorRate    错误率
     * @return 依赖健康度 Map
     */
    private static Map<String, Object> dependency(String service, String dependsOn, String status,
                                                  double latencyP95Ms, double errorRate) {
        // 使用调用方 -> 被依赖方的语义组装快照
        return map(
                "service", service,
                "dependsOn", dependsOn,
                "status", status,
                "latencyP95Ms", latencyP95Ms,
                "errorRate", errorRate
        );
    }

    /**
     * 组装原始信号包。
     *
     * @param deployments  发布记录
     * @param metrics      指标采样点
     * @param logs         日志事件
     * @param dependencies 依赖健康度
     * @return 信号包 Map
     */
    private static Map<String, Object> signals(List<Map<String, Object>> deployments,
                                               List<Map<String, Object>> metrics,
                                               List<Map<String, Object>> logs,
                                               List<Map<String, Object>> dependencies) {
        // 四个列表与 IncidentSignals 的字段一一对应
        return map(
                "deployments", deployments,
                "metrics", metrics,
                "logs", logs,
                "dependencies", dependencies
        );
    }

    /**
     * 构造一条标准答案。
     *
     * @param category      根因分类
     * @param summary       根因摘要
     * @param evidenceTypes 期望证据类型
     * @param actionType    期望动作类型
     * @param risk          期望风险等级
     * @param escalate      是否需要升级人工
     * @param notes         评分说明
     * @return 标准答案 Map
     */
    private static Map<String, Object> groundTruth(String category, String summary, List<String> evidenceTypes,
                                                   String actionType, String risk, boolean escalate, String notes) {
        // 字段名必须与 GroundTruth record 的组件名完全一致，才能被 Jackson 反序列化
        return map(
                "rootCauseCategory", category,
                "rootCauseSummary", summary,
                "expectedEvidenceTypes", evidenceTypes,
                "expectedActionType", actionType,
                "expectedActionRisk", risk,
                "shouldEscalate", escalate,
                "notes", notes
        );
    }

    /**
     * 按键值对顺序构造有序 Map。
     *
     * @param keyValues 交替出现的 key 和 value
     * @return 保持插入顺序的 Map
     */
    private static Map<String, Object> map(Object... keyValues) {
        // 使用 LinkedHashMap 保证 JSON 字段顺序稳定，便于人工阅读和 diff
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}

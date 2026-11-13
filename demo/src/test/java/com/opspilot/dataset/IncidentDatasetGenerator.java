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
 * Generates the synthetic incident dataset used by the evaluation harness.
 *
 * <p>Run with:
 * <pre>
 * ./mvnw -Dtest=IncidentDatasetGeneratorTest -Dopspilot.generateDataset=true test
 * </pre>
 */
public final class IncidentDatasetGenerator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Instant BASE = Instant.parse("2026-11-10T08:00:00Z");

    private IncidentDatasetGenerator() {
    }

    public static void main(String[] args) throws Exception {
        Path output = args.length > 0
                ? Path.of(args[0])
                : Path.of("..", "demo-data", "incidents.jsonl").normalize();
        generate(output);
        System.out.println("Wrote 50 incidents to " + output.toAbsolutePath());
    }

    public static void generate(Path output) throws Exception {
        List<Map<String, Object>> cases = new ArrayList<>(50);
        int caseNo = 1;
        caseNo = addBadDeploy(cases, caseNo);
        caseNo = addDependencyTimeout(cases, caseNo);
        caseNo = addConnectionPoolExhaustion(cases, caseNo);
        caseNo = addTrafficSpike(cases, caseNo);
        addConfigError(cases, caseNo);

        Files.createDirectories(output.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (Map<String, Object> incident : cases) {
                writer.write(MAPPER.writeValueAsString(incident));
                writer.write('\n');
            }
        }
    }

    private static int addBadDeploy(List<Map<String, Object>> cases, int no) {
        String[] services = {
                "order-service", "payment-service", "user-service", "cart-service", "search-service",
                "notification-service", "inventory-service", "api-gateway", "coupon-service", "review-service"
        };
        for (int i = 0; i < 10; i++) {
            int caseNo = no + i;
            String service = services[i];
            Instant firedAt = BASE.plusSeconds(i * 3600L);
            Instant deployedAt = firedAt.minusSeconds(15 * 60L);
            String version = "v1.8." + i;

            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, version, deployedAt,
                            "refactor payment callback", "upgrade http client")
            );
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.12 + i * 0.01, "ratio",
                            map("status", "5xx")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 1800 + i * 120, "ms",
                            map("status", "all")),
                    metric("jvm.cpu.usage", service, firedAt.minusSeconds(60), 0.55 + i * 0.01, "ratio", map())
            );
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "unexpected exception in request handler", map("endpoint", "/api/orders")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "payment callback timeout after 3000ms", map("paymentId", "pay_" + caseNo))
            );
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "payment-service", "UP", 120 + i * 5, 0.01)
            );

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

            cases.add(caseMap(caseNo, "BAD_DEPLOY", service,
                    alert(caseNo, service, "HTTP 5xx rate > 10% for 5m",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " 5xx rate " + (12 + i) + "%, p95 " + (1800 + i * 120) + "ms"),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        return no + 10;
    }

    private static int addDependencyTimeout(List<Map<String, Object>> cases, int no) {
        String[] services = {
                "order-service", "payment-service", "checkout-service", "cart-service", "inventory-service",
                "notification-service", "search-service", "user-service", "coupon-service", "review-service"
        };
        for (int i = 0; i < 10; i++) {
            int caseNo = no + i;
            String service = services[i];
            Instant firedAt = BASE.plusSeconds(24 * 3600L + i * 3600L);
            Instant deployedAt = firedAt.minusSeconds(3 * 24 * 3600L);

            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.7." + i, deployedAt,
                            "routine dependency bump", "logging cleanup")
            );
            List<Map<String, Object>> metrics = List.of(
                    metric("http.client.requests.p95", service, firedAt.minusSeconds(60), 2500 + i * 100, "ms",
                            map("dependency", "payment-service")),
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.08 + i * 0.005, "ratio",
                            map("status", "5xx")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 2200 + i * 80, "ms",
                            map("status", "all"))
            );
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "Read timed out after 3000ms calling payment-service",
                            map("dependency", "payment-service")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "WARN",
                            "circuit breaker open for payment-service", map("dependency", "payment-service"))
            );
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

            cases.add(caseMap(caseNo, "DEPENDENCY_TIMEOUT", service,
                    alert(caseNo, service, "p95 latency > 3s for 5m",
                            i < 5 ? "P1" : "P2", firedAt,
                            service + " p95 " + (2500 + i * 100) + "ms, downstream payment-service degraded"),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        return no + 10;
    }

    private static int addConnectionPoolExhaustion(List<Map<String, Object>> cases, int no) {
        String[] services = {
                "order-service", "payment-service", "user-service", "inventory-service", "coupon-service",
                "notification-service", "search-service", "cart-service", "review-service", "api-gateway"
        };
        for (int i = 0; i < 10; i++) {
            int caseNo = no + i;
            String service = services[i];
            Instant firedAt = BASE.plusSeconds(48 * 3600L + i * 3600L);
            Instant deployedAt = firedAt.minusSeconds(2 * 24 * 3600L);

            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.6." + i, deployedAt,
                            "query refactor", "add new repository method")
            );
            List<Map<String, Object>> metrics = List.of(
                    metric("db.connection.active", service, firedAt.minusSeconds(60), 20 + i, "count", map("pool", "primary")),
                    metric("db.connection.pending", service, firedAt.minusSeconds(60), 5 + i, "count", map("pool", "primary")),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 3000 + i * 100, "ms", map()),
                    metric("db.query.p95", service, firedAt.minusSeconds(60), 800 + i * 50, "ms", map("db", "primary"))
            );
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "HikariPool-1 - Connection is not available, request timed out after 30000ms",
                            map("pool", "HikariPool-1")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "WARN",
                            i >= 7 ? "Connection leak detection triggered" : "Slow query detected: SELECT order by id",
                            map("threshold", "1000ms"))
            );
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "primary-db", "DEGRADED", 900 + i * 50, 0.02)
            );

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

            cases.add(caseMap(caseNo, "CONNECTION_POOL_EXHAUSTION", service,
                    alert(caseNo, service, "DB connection pool exhausted",
                            i < 5 ? "P1" : "P2", firedAt,
                            service + " active=" + (20 + i) + ", pending=" + (5 + i)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        return no + 10;
    }

    private static int addTrafficSpike(List<Map<String, Object>> cases, int no) {
        String[] services = {
                "api-gateway", "order-service", "search-service", "product-service", "cart-service",
                "payment-service", "user-service", "notification-service", "review-service", "coupon-service"
        };
        for (int i = 0; i < 10; i++) {
            int caseNo = no + i;
            String service = services[i];
            Instant firedAt = BASE.plusSeconds(72 * 3600L + i * 3600L);
            Instant deployedAt = firedAt.minusSeconds(4 * 24 * 3600L);

            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, "v1.5." + i, deployedAt,
                            "routine release", "dependency updates")
            );
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.qps", service, firedAt.minusSeconds(60), 5000 + i * 800, "qps", map()),
                    metric("jvm.cpu.usage", service, firedAt.minusSeconds(60), Math.min(0.98, 0.82 + i * 0.01), "ratio", map()),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 2000 + i * 100, "ms", map()),
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.02 + i * 0.002, "ratio",
                            map("status", "5xx"))
            );
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "WARN",
                            "rate limit exceeded for client campaign-2026-11", map("client", "campaign")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "queue full, rejecting request", map("queue", "request-queue"))
            );
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "primary-db", "UP", 150 + i * 10, 0.01)
            );

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

            cases.add(caseMap(caseNo, "TRAFFIC_SPIKE", service,
                    alert(caseNo, service, "Request rate 5x baseline",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " qps=" + (5000 + i * 800) + ", cpu=" + Math.min(0.98, 0.82 + i * 0.01)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        return no + 10;
    }

    private static int addConfigError(List<Map<String, Object>> cases, int no) {
        String[] services = {
                "payment-service", "order-service", "notification-service", "user-service", "search-service",
                "inventory-service", "coupon-service", "api-gateway", "review-service", "cart-service"
        };
        for (int i = 0; i < 10; i++) {
            int caseNo = no + i;
            String service = services[i];
            Instant firedAt = BASE.plusSeconds(96 * 3600L + i * 3600L);
            Instant changedAt = firedAt.minusSeconds(10 * 60L);
            String version = "config-2026.11.10-" + (i + 1);

            List<Map<String, Object>> deployments = List.of(
                    deployment(caseNo, service, version, changedAt,
                            "update payment.timeout", "enable new feature flag")
            );
            List<Map<String, Object>> metrics = List.of(
                    metric("http.server.requests.error_rate", service, firedAt.minusSeconds(60), 0.15 + i * 0.01, "ratio",
                            map("status", "5xx")),
                    metric("config.refresh.failures", service, firedAt.minusSeconds(60), 1 + i, "count", map()),
                    metric("http.server.requests.p95", service, firedAt.minusSeconds(60), 1500 + i * 100, "ms", map())
            );
            List<Map<String, Object>> logs = List.of(
                    log(caseNo, 1, service, firedAt.minusSeconds(120), "ERROR",
                            "Invalid configuration value for payment.timeout: expected duration, got '3s'",
                            map("property", "payment.timeout")),
                    log(caseNo, 2, service, firedAt.minusSeconds(90), "ERROR",
                            "Failed to bind configuration properties", map("property", "payment.retry.count"))
            );
            List<Map<String, Object>> dependencies = List.of(
                    dependency(service, "config-service", "UP", 80 + i * 5, 0.01)
            );

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

            cases.add(caseMap(caseNo, "CONFIG_ERROR", service,
                    alert(caseNo, service, "5xx rate > 15% after config change",
                            i < 6 ? "P1" : "P2", firedAt,
                            service + " config refresh failures=" + (1 + i)),
                    signals(deployments, metrics, logs, dependencies),
                    groundTruth));
        }
        return no + 10;
    }

    private static Map<String, Object> caseMap(int caseNo, String category, String service,
                                               Map<String, Object> alert,
                                               Map<String, Object> signals,
                                               Map<String, Object> groundTruth) {
        return map(
                "caseId", String.format("CASE-%03d", caseNo),
                "category", category,
                "service", service,
                "alert", alert,
                "signals", signals,
                "groundTruth", groundTruth
        );
    }

    private static Map<String, Object> alert(int caseNo, String service, String symptom, String severity,
                                             Instant firedAt, String description) {
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

    private static Map<String, Object> deployment(int caseNo, String service, String version, Instant deployedAt,
                                                  String... changes) {
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

    private static Map<String, Object> metric(String name, String service, Instant timestamp, double value,
                                              String unit, Map<String, ?> labels) {
        return map(
                "name", name,
                "service", service,
                "timestamp", timestamp.toString(),
                "value", value,
                "unit", unit,
                "labels", labels
        );
    }

    private static Map<String, Object> log(int caseNo, int index, String service, Instant timestamp,
                                           String level, String message, Map<String, ?> attributes) {
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

    private static Map<String, Object> dependency(String service, String dependsOn, String status,
                                                  double latencyP95Ms, double errorRate) {
        return map(
                "service", service,
                "dependsOn", dependsOn,
                "status", status,
                "latencyP95Ms", latencyP95Ms,
                "errorRate", errorRate
        );
    }

    private static Map<String, Object> signals(List<Map<String, Object>> deployments,
                                               List<Map<String, Object>> metrics,
                                               List<Map<String, Object>> logs,
                                               List<Map<String, Object>> dependencies) {
        return map(
                "deployments", deployments,
                "metrics", metrics,
                "logs", logs,
                "dependencies", dependencies
        );
    }

    private static Map<String, Object> groundTruth(String category, String summary, List<String> evidenceTypes,
                                                   String actionType, String risk, boolean escalate, String notes) {
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

    private static Map<String, Object> map(Object... keyValues) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            result.put((String) keyValues[i], keyValues[i + 1]);
        }
        return result;
    }
}

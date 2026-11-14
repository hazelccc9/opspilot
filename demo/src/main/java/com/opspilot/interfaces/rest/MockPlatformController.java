package com.opspilot.interfaces.rest;

import com.opspilot.application.mock.MockCaseSummary;
import com.opspilot.application.mock.MockFaultActivation;
import com.opspilot.application.mock.MockPlatformService;
import com.opspilot.domain.enums.LogLevel;
import com.opspilot.domain.model.Alert;
import com.opspilot.domain.model.DependencyStatus;
import com.opspilot.domain.model.Deployment;
import com.opspilot.domain.model.LogEvent;
import com.opspilot.domain.model.MetricSample;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * Mock 平台 API：模拟外部监控、日志、发布和 CMDB 系统。
 *
 * <p>这些接口只返回原始信号，不返回评测用的 {@code groundTruth}。
 * 后续 Agent 工具会通过这些接口查询数据。
 */
@RestController
@RequestMapping("/mock")
public class MockPlatformController {

    private final MockPlatformService mockPlatformService;

    public MockPlatformController(MockPlatformService mockPlatformService) {
        this.mockPlatformService = mockPlatformService;
    }

    @GetMapping("/cases")
    public List<MockCaseSummary> listCases() {
        return mockPlatformService.listCases();
    }

    @GetMapping("/alerts/{alertId}")
    public Alert getAlert(@PathVariable String alertId) {
        return mockPlatformService.getAlert(alertId);
    }

    @GetMapping("/metrics")
    public List<MetricSample> queryMetrics(
            @RequestParam String service,
            @RequestParam(required = false) String name,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return mockPlatformService.queryMetrics(service, name, from, to);
    }

    @GetMapping("/logs")
    public List<LogEvent> queryLogs(
            @RequestParam String service,
            @RequestParam(required = false) String query,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) LogLevel level,
            @RequestParam(defaultValue = "100") int limit) {
        return mockPlatformService.queryLogs(service, query, from, to, level, limit);
    }

    @GetMapping("/deployments")
    public List<Deployment> queryDeployments(
            @RequestParam String service,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return mockPlatformService.queryDeployments(service, from, to);
    }

    @GetMapping("/dependencies")
    public List<DependencyStatus> queryDependencies(
            @RequestParam String service,
            @RequestParam(required = false) String dependsOn) {
        return mockPlatformService.queryDependencies(service, dependsOn);
    }

    /** 激活一条故障用例，返回它产生的告警。 */
    @PostMapping("/faults/{caseId}/activate")
    public MockFaultActivation activate(@PathVariable String caseId) {
        return mockPlatformService.activate(caseId);
    }

    /** 查询当前激活的故障；没有激活时返回 204。 */
    @GetMapping("/faults/active")
    public ResponseEntity<MockFaultActivation> activeFault() {
        return mockPlatformService.activeCase()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** 清除当前激活的故障。 */
    @PostMapping("/faults/reset")
    @ResponseStatus(NO_CONTENT)
    public void reset() {
        mockPlatformService.reset();
    }
}

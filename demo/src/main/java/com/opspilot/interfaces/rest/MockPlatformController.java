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
 * Mock 平台 API，模拟外部监控、日志、发布和 CMDB 系统。
 *
 * 这些接口只返回原始信号，不返回评测用的 {@code groundTruth}。
 * 后续 Agent 工具会通过这些接口查询数据。
 */
@RestController
@RequestMapping("/mock")
public class MockPlatformController {

    /**
     * Mock 平台服务，模拟外部监控、日志、发布和 CMDB 的只读数据源，
     * 供 Agent 工具和评测查询原始信号。
     */
    private final MockPlatformService mockPlatformService;

    /**
     * 构造 Mock 平台 Controller。
     *
     * @param mockPlatformService Mock 平台查询服务
     */
    public MockPlatformController(MockPlatformService mockPlatformService) {
        // 注入 Mock 平台查询服务，所有查询和状态操作都委托给它
        this.mockPlatformService = mockPlatformService;
    }

    /**
     * 列出所有可用的故障用例摘要。
     *
     * @return 用例摘要列表，不含 ground truth
     */
    @GetMapping("/cases")
    public List<MockCaseSummary> listCases() {
        // 直接委托应用服务查询，Controller 不额外处理数据
        return mockPlatformService.listCases();
    }

    /**
     * 按告警 ID 查询告警。
     *
     * @param alertId 告警 ID
     * @return 告警快照
     */
    @GetMapping("/alerts/{alertId}")
    public Alert getAlert(@PathVariable String alertId) {
        // 告警不存在时由应用服务抛出异常，统一交给 ApiExceptionHandler 转 404
        return mockPlatformService.getAlert(alertId);
    }

    /**
     * 查询指标采样点。
     *
     * @param service 服务名，必填
     * @param name    可选指标名
     * @param from    可选起始时间（ISO-8601）
     * @param to      可选结束时间（ISO-8601）
     * @return 指标采样点列表
     */
    @GetMapping("/metrics")
    public List<MetricSample> queryMetrics(
            @RequestParam String service,
            @RequestParam(required = false) String name,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        // 时间窗校验和过滤逻辑都在应用服务中完成
        return mockPlatformService.queryMetrics(service, name, from, to);
    }

    /**
     * 查询日志事件。
     *
     * @param service 服务名，必填
     * @param query   可选关键字
     * @param from    可选起始时间（ISO-8601）
     * @param to      可选结束时间（ISO-8601）
     * @param level   可选日志级别
     * @param limit   返回条数，默认 100
     * @return 日志事件列表，按时间倒序
     */
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
        // 关键字匹配、级别过滤和条数上限都由应用服务处理
        return mockPlatformService.queryLogs(service, query, from, to, level, limit);
    }

    /**
     * 查询发布/配置变更记录。
     *
     * @param service 服务名，必填
     * @param from    可选起始时间（ISO-8601）
     * @param to      可选结束时间（ISO-8601）
     * @return 发布记录列表，按时间倒序
     */
    @GetMapping("/deployments")
    public List<Deployment> queryDeployments(
            @RequestParam String service,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        // 直接委托应用服务按服务和变更时间窗查询
        return mockPlatformService.queryDeployments(service, from, to);
    }

    /**
     * 查询依赖健康度快照。
     *
     * @param service   调用方服务名，必填
     * @param dependsOn 可选被依赖服务名
     * @return 依赖健康度列表
     */
    @GetMapping("/dependencies")
    public List<DependencyStatus> queryDependencies(
            @RequestParam String service,
            @RequestParam(required = false) String dependsOn) {
        // 应用服务负责按调用方和被依赖方过滤
        return mockPlatformService.queryDependencies(service, dependsOn);
    }

    /**
     * 激活一条故障用例，返回它产生的告警。
     *
     * @param caseId 用例 ID
     * @return 激活结果
     */
    @PostMapping("/faults/{caseId}/activate")
    public MockFaultActivation activate(@PathVariable String caseId) {
        // 用例不存在时由应用服务抛出异常，统一转换为 404
        return mockPlatformService.activate(caseId);
    }

    /**
     * 查询当前激活的故障；没有激活时返回 204。
     *
     * @return 当前激活结果，或 204 No Content
     */
    @GetMapping("/faults/active")
    public ResponseEntity<MockFaultActivation> activeFault() {
        // 有激活用例时返回 200 + 内容，否则返回 204
        return mockPlatformService.activeCase()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * 清除当前激活的故障。
     */
    @PostMapping("/faults/reset")
    @ResponseStatus(NO_CONTENT)
    public void reset() {
        // 清空服务端状态，接口固定返回 204
        mockPlatformService.reset();
    }
}

package com.opspilot.interfaces.rest;

import com.opspilot.application.incident.IncidentService;
import com.opspilot.domain.enums.IncidentStatus;
import com.opspilot.interfaces.rest.dto.IncidentResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 故障查询 API
 *
 * 只负责 HTTP 层参数适配，查询逻辑委托给 {@link IncidentService}。
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    /**
     * 故障应用服务，提供按 ID 查询、按状态/服务过滤的故障列表。
     */
    private final IncidentService incidentService;

    /**
     * 构造故障查询 Controller。
     *
     * @param incidentService 故障应用服务
     */
    public IncidentController(IncidentService incidentService) {
        // 注入故障应用服务，查询逻辑统一委托给它
        this.incidentService = incidentService;
    }

    /**
     * 按故障 ID 查询故障详情。
     *
     * @param incidentId 故障 ID
     * @return 故障响应
     */
    @GetMapping("/{incidentId}")
    public IncidentResponse get(@PathVariable String incidentId) {
        // 查询领域对象并转换为对外响应，避免直接暴露领域模型
        return IncidentResponse.from(incidentService.getIncident(incidentId));
    }

    /**
     * 查询故障列表，支持按状态和服务过滤。
     *
     * @param status  可选状态；为 null 时不过滤
     * @param service 可选服务名；为 null 或空字符串时不过滤
     * @return 故障响应列表
     */
    @GetMapping
    public List<IncidentResponse> list(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) String service) {
        // 应用服务负责过滤和排序，Controller 只做领域对象到响应 DTO 的映射
        return incidentService.listIncidents(status, service).stream()
                .map(IncidentResponse::from)
                .toList();
    }
}

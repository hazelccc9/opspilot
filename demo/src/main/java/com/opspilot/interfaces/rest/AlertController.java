package com.opspilot.interfaces.rest;

import com.opspilot.application.incident.IncidentService;
import com.opspilot.domain.model.Alert;
import com.opspilot.domain.model.Incident;
import com.opspilot.interfaces.rest.dto.CreateAlertRequest;
import com.opspilot.interfaces.rest.dto.IncidentResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Map;

/**
 * 告警接入 API
 *
 * 当前只负责“告警 -> 故障”的建单；调查接口等 Agent 运行时落地后再开放。
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    /**
     * 故障应用服务，负责把告警转成故障事件并持久化，Controller 只做 HTTP 层适配。
     */
    private final IncidentService incidentService;

    /**
     * 构造告警接入 Controller。
     *
     * @param incidentService 故障应用服务
     */
    public AlertController(IncidentService incidentService) {
        // 注入故障应用服务，Controller 只负责 HTTP 适配
        this.incidentService = incidentService;
    }

    /**
     * 接收一条告警并创建对应故障。
     *
     * @param request 告警请求体，已通过 Bean Validation 校验
     * @return 201 Created，Location 指向新建故障，响应体为故障详情
     */
    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody CreateAlertRequest request) {
        // 把 HTTP 请求 DTO 转换为领域告警对象，避免领域层依赖 Web 契约
        Alert alert = new Alert(
                request.alertId(),
                request.service(),
                request.symptom(),
                request.severity(),
                request.firedAt(),
                request.source(),
                request.description(),
                request.labels() == null ? Map.of() : request.labels()
        );

        // 交给应用服务完成建单和幂等校验
        Incident incident = incidentService.createIncident(alert);

        // 返回 201，并通过 Location 头暴露新故障的查询地址
        return ResponseEntity
                .created(URI.create("/api/incidents/" + incident.incidentId()))
                .body(IncidentResponse.from(incident));
    }
}

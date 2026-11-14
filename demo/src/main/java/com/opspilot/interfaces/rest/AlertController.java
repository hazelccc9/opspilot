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
 * 告警接入 API。
 *
 * <p>当前只负责“告警 -> 故障”的建单；调查接口等 Agent 运行时落地后再开放。
 */
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final IncidentService incidentService;

    public AlertController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody CreateAlertRequest request) {
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

        Incident incident = incidentService.createIncident(alert);

        return ResponseEntity
                .created(URI.create("/api/incidents/" + incident.incidentId()))
                .body(IncidentResponse.from(incident));
    }
}

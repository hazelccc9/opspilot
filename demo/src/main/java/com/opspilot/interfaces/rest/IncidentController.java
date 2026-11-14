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

/** 故障查询 API。 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping("/{incidentId}")
    public IncidentResponse get(@PathVariable String incidentId) {
        return IncidentResponse.from(incidentService.getIncident(incidentId));
    }

    @GetMapping
    public List<IncidentResponse> list(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) String service) {
        return incidentService.listIncidents(status, service).stream()
                .map(IncidentResponse::from)
                .toList();
    }
}

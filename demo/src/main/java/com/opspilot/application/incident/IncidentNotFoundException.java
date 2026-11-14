package com.opspilot.application.incident;

/** 请求的故障不存在。 */
public class IncidentNotFoundException extends RuntimeException {

    public IncidentNotFoundException(String incidentId) {
        super("Incident not found: " + incidentId);
    }
}

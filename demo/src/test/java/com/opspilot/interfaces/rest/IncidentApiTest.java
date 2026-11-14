package com.opspilot.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class IncidentApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsIncidentAndReadsItBack() throws Exception {
        String alertId = "ALERT-IT-" + UUID.randomUUID();
        String body = alertBody(alertId, "order-service");

        MvcResult result = mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.alertId").value(alertId))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(alertId))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void duplicateAlertReturns409() throws Exception {
        String alertId = "ALERT-IT-DUP-" + UUID.randomUUID();
        String body = alertBody(alertId, "payment-service");

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate alert"));
    }

    @Test
    void invalidRequestReturns400WithFieldErrors() throws Exception {
        String body = """
                {
                  "alertId": "ALERT-IT-INVALID",
                  "symptom": "5xx spike",
                  "severity": "P1",
                  "firedAt": "2026-11-10T08:00:00Z",
                  "source": "prometheus",
                  "description": "invalid request"
                }
                """;

        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.service").exists());
    }

    @Test
    void unknownIncidentReturns404() throws Exception {
        mockMvc.perform(get("/api/incidents/INC-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    private static String alertBody(String alertId, String service) {
        return """
                {
                  "alertId": "%s",
                  "service": "%s",
                  "symptom": "HTTP 5xx rate > 10%% for 5m",
                  "severity": "P1",
                  "firedAt": "2026-11-10T08:00:00Z",
                  "source": "prometheus",
                  "description": "5xx rate 12%%",
                  "labels": { "env": "prod" }
                }
                """.formatted(alertId, service);
    }
}

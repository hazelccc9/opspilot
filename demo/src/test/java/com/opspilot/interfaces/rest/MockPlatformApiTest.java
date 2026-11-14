package com.opspilot.interfaces.rest;

import com.opspilot.application.mock.MockPlatformService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MockPlatformApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MockPlatformService mockPlatformService;

    @AfterEach
    void resetFault() {
        mockPlatformService.reset();
    }

    @Test
    void loadsFiftyCasesWithoutGroundTruth() throws Exception {
        mockMvc.perform(get("/mock/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50))
                .andExpect(jsonPath("$[0].caseId").value("CASE-001"))
                .andExpect(jsonPath("$[0].groundTruth").doesNotExist());
    }

    @Test
    void getsAlertById() throws Exception {
        mockMvc.perform(get("/mock/alerts/ALERT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value("ALERT-001"))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.severity").value("P1"));
    }

    @Test
    void queriesMetricsByServiceAndName() throws Exception {
        mockMvc.perform(get("/mock/metrics")
                        .param("service", "order-service")
                        .param("name", "http.server.requests.error_rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].name").value("http.server.requests.error_rate"));
    }

    @Test
    void queriesErrorLogs() throws Exception {
        mockMvc.perform(get("/mock/logs")
                        .param("service", "order-service")
                        .param("level", "ERROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].level").value("ERROR"));
    }

    @Test
    void queriesDeployments() throws Exception {
        mockMvc.perform(get("/mock/deployments")
                        .param("service", "order-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].version").exists());
    }

    @Test
    void queriesDependencies() throws Exception {
        mockMvc.perform(get("/mock/dependencies")
                        .param("service", "order-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].dependsOn").exists());
    }

    @Test
    void activatesFaultAndReturnsItsAlert() throws Exception {
        mockMvc.perform(post("/mock/faults/CASE-001/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value("CASE-001"))
                .andExpect(jsonPath("$.alert.alertId").value("ALERT-001"));

        mockMvc.perform(get("/mock/faults/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value("CASE-001"));
    }

    @Test
    void resetsActiveFault() throws Exception {
        mockMvc.perform(post("/mock/faults/CASE-001/activate"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/mock/faults/reset"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/mock/faults/active"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unknownAlertReturns404() throws Exception {
        mockMvc.perform(get("/mock/alerts/ALERT-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void invalidTimeRangeReturns400() throws Exception {
        mockMvc.perform(get("/mock/metrics")
                        .param("service", "order-service")
                        .param("from", "2026-11-11T00:00:00Z")
                        .param("to", "2026-11-10T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void missingServiceReturns400() throws Exception {
        mockMvc.perform(get("/mock/metrics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing request parameter"));
    }
}

package com.opspilot.dataset;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opspilot.domain.model.IncidentCase;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentDatasetTest {

    private static final Path DATASET = DatasetPaths.incidents();

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    @Test
    void datasetHasFiftyCasesAndTenPerCategory() throws Exception {
        List<IncidentCase> cases = load();

        assertEquals(50, cases.size());

        Map<String, Long> counts = cases.stream()
                .collect(Collectors.groupingBy(c -> c.category().name(), Collectors.counting()));

        for (String category : List.of(
                "BAD_DEPLOY",
                "DEPENDENCY_TIMEOUT",
                "CONNECTION_POOL_EXHAUSTION",
                "TRAFFIC_SPIKE",
                "CONFIG_ERROR")) {
            assertEquals(10L, counts.getOrDefault(category, 0L), category);
        }
    }

    @Test
    void caseIdsAndAlertIdsAreUnique() throws Exception {
        List<IncidentCase> cases = load();

        assertEquals(cases.size(), cases.stream().map(IncidentCase::caseId).distinct().count());
        assertEquals(cases.size(), cases.stream().map(c -> c.alert().alertId()).distinct().count());
    }

    @Test
    void everyCaseHasGroundTruthAndSignals() throws Exception {
        for (IncidentCase incident : load()) {
            assertNotNull(incident.alert(), incident.caseId());
            assertNotNull(incident.signals(), incident.caseId());
            assertNotNull(incident.groundTruth(), incident.caseId());
            assertEquals(incident.category(), incident.groundTruth().rootCauseCategory(), incident.caseId());

            assertFalse(incident.signals().metrics().isEmpty(), incident.caseId());
            assertFalse(incident.signals().logs().isEmpty(), incident.caseId());
            assertFalse(incident.groundTruth().expectedEvidenceTypes().isEmpty(), incident.caseId());
            assertNotNull(incident.groundTruth().expectedActionType(), incident.caseId());
            assertNotNull(incident.groundTruth().expectedActionRisk(), incident.caseId());
        }
    }

    @Test
    void everyCaseHasDeploymentOrDependencySignal() throws Exception {
        for (IncidentCase incident : load()) {
            assertTrue(
                    !incident.signals().deployments().isEmpty()
                            || !incident.signals().dependencies().isEmpty(),
                    incident.caseId()
            );
        }
    }

    private List<IncidentCase> load() throws Exception {
        assertTrue(Files.exists(DATASET), "Dataset not found: " + DATASET);

        List<IncidentCase> cases = new ArrayList<>();
        for (String line : Files.readAllLines(DATASET, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                cases.add(MAPPER.readValue(line, IncidentCase.class));
            }
        }
        return cases;
    }
}

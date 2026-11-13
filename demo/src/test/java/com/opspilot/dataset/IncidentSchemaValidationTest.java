package com.opspilot.dataset;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentSchemaValidationTest {

    private static final Path DATASET = DatasetPaths.incidents();
    private static final Path SCHEMA = DatasetPaths.schema();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void everyIncidentMatchesJsonSchema() throws Exception {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(SCHEMA.toUri());

        List<String> lines = Files.readAllLines(DATASET, StandardCharsets.UTF_8)
                .stream()
                .filter(line -> !line.isBlank())
                .toList();

        for (String line : lines) {
            JsonNode incident = MAPPER.readTree(line);
            Set<ValidationMessage> errors = schema.validate(incident);
            assertTrue(errors.isEmpty(),
                    () -> incident.path("caseId").asText() + " schema errors: " + errors);
        }
    }
}

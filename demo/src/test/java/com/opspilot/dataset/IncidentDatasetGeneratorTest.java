package com.opspilot.dataset;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IncidentDatasetGeneratorTest {

    @Test
    void generateDatasetWhenRequested() throws Exception {
        if (!Boolean.getBoolean("opspilot.generateDataset")) {
            return;
        }
        Path output = DatasetPaths.incidents();
        IncidentDatasetGenerator.generate(output);
        assertTrue(Files.exists(output));
        assertTrue(Files.size(output) > 0);
    }
}

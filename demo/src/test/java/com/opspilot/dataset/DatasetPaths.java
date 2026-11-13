package com.opspilot.dataset;

import java.nio.file.Path;

/**
 * Resolves the dataset location for tests and the generator.
 *
 * <p>Maven passes {@code opspilot.data.dir}; the fallback keeps the test runnable
 * from an IDE where the working directory is the {@code demo} module.
 */
final class DatasetPaths {

    private static final String DATA_DIR_PROPERTY = "opspilot.data.dir";

    private DatasetPaths() {
    }

    static Path dataDir() {
        String configured = System.getProperty(DATA_DIR_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        return Path.of(System.getProperty("user.dir"))
                .resolve("..")
                .resolve("demo-data")
                .toAbsolutePath()
                .normalize();
    }

    static Path incidents() {
        return dataDir().resolve("incidents.jsonl");
    }

    static Path schema() {
        return dataDir().resolve("schema").resolve("incident.schema.json");
    }
}

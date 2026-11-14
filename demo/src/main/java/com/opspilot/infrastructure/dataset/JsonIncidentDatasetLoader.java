package com.opspilot.infrastructure.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opspilot.domain.model.IncidentCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 从 JSONL 文件加载 50 条合成故障用例。
 *
 * <p>加载结果只用于 Mock 平台和评测，不会把 {@code groundTruth} 暴露给 Agent。
 * 路径优先使用配置值，找不到时回退到仓库根目录/模块目录下的 {@code demo-data}，
 * 这样从仓库根目录或 {@code demo} 目录启动都能工作。
 */
@Component
public class JsonIncidentDatasetLoader {

    private static final Logger log = LoggerFactory.getLogger(JsonIncidentDatasetLoader.class);

    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    private final String configuredPath;

    public JsonIncidentDatasetLoader(
            @Value("${opspilot.mock-platform.dataset-path:../demo-data/incidents.jsonl}")
            String configuredPath) {
        this.configuredPath = configuredPath;
    }

    /**
     * 读取并解析整个数据集。
     *
     * @throws IllegalStateException 文件不存在、读取失败或 JSON 无法解析
     */
    public List<IncidentCase> load() {
        Path path = resolvePath();
        List<IncidentCase> cases = new ArrayList<>();

        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.filter(line -> !line.isBlank())
                    .map(this::readCase)
                    .forEach(cases::add);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read mock dataset: " + path, exception);
        }

        if (cases.isEmpty()) {
            throw new IllegalStateException("Mock dataset is empty: " + path);
        }

        log.info("Loaded {} mock incident cases from {}", cases.size(), path);
        return List.copyOf(cases);
    }

    private IncidentCase readCase(String line) {
        try {
            return objectMapper.readValue(line, IncidentCase.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Invalid incident JSON: " + line, exception);
        }
    }

    private Path resolvePath() {
        List<Path> candidates = new ArrayList<>();
        if (configuredPath != null && !configuredPath.isBlank()) {
            candidates.add(Path.of(configuredPath));
        }
        candidates.add(Path.of("demo-data", "incidents.jsonl"));
        candidates.add(Path.of("..", "demo-data", "incidents.jsonl"));

        List<Path> tried = new ArrayList<>();
        for (Path candidate : candidates) {
            Path absolute = candidate.toAbsolutePath().normalize();
            tried.add(absolute);
            if (Files.isRegularFile(absolute)) {
                return absolute;
            }
        }

        throw new IllegalStateException("Mock dataset not found. Tried: " + tried);
    }
}

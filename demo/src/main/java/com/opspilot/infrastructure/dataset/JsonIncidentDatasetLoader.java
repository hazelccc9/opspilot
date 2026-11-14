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
 * JSONL 合成故障用例加载器
 *
 * 加载结果只用于 Mock 平台和评测，不会把 {@code groundTruth} 暴露给 Agent。
 * 路径优先使用配置值，找不到时回退到仓库根目录/模块目录下的 {@code demo-data}，
 * 这样从仓库根目录或 {@code demo} 目录启动都能工作。
 */
@Component
public class JsonIncidentDatasetLoader {

    /** 数据集加载过程的日志记录器，用于输出加载数量和文件位置。 */
    private static final Logger log = LoggerFactory.getLogger(JsonIncidentDatasetLoader.class);

    /** JSONL 行反序列化器，支持 Java 时间类型并忽略未知字段。 */
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    /** 配置项 {@code opspilot.mock-platform.dataset-path} 指定的 JSONL 路径；为空时走默认搜索路径。 */
    private final String configuredPath;

    /**
     * 构造数据集加载器。
     *
     * @param configuredPath 配置项指定的 JSONL 路径，为空时使用默认搜索路径
     */
    public JsonIncidentDatasetLoader(
            @Value("${opspilot.mock-platform.dataset-path:../demo-data/incidents.jsonl}")
            String configuredPath) {
        // 保存配置路径，实际文件定位推迟到 load() 时执行
        this.configuredPath = configuredPath;
    }

    /**
     * 读取并解析整个数据集。
     *
     * @return 不可变的故障用例列表
     * @throws IllegalStateException 文件不存在、读取失败或 JSON 无法解析
     */
    public List<IncidentCase> load() {
        // 先定位文件；找不到时 resolvePath 会直接抛出包含候选路径的异常
        Path path = resolvePath();
        List<IncidentCase> cases = new ArrayList<>();

        // 按行流式读取并逐行解析；try-with-resources 保证文件句柄必然关闭
        try (Stream<String> lines = Files.lines(path, StandardCharsets.UTF_8)) {
            lines.filter(line -> !line.isBlank())
                    .map(this::readCase)
                    .forEach(cases::add);
        } catch (IOException exception) {
            // 权限、编码、文件被占用等 IO 问题统一包装成启动期错误
            throw new IllegalStateException("Failed to read mock dataset: " + path, exception);
        }

        // 空数据集通常意味着路径指错或文件被清空，尽早失败而不是带着空数据启动
        if (cases.isEmpty()) {
            throw new IllegalStateException("Mock dataset is empty: " + path);
        }

        // 记录加载结果，方便确认启动时读取的是哪份数据集
        log.info("Loaded {} mock incident cases from {}", cases.size(), path);
        // 返回不可变列表，防止后续代码意外修改已加载的数据
        return List.copyOf(cases);
    }

    /**
     * 把一行 JSONL 文本反序列化成一条故障用例。
     *
     * 任何一行解析失败都立即抛出异常，避免带着半截数据继续启动。
     *
     * @param line JSONL 中的单行文本，调用方已过滤空行
     * @return 解析后的故障用例
     * @throws IllegalStateException 当该行无法反序列化为 {@link IncidentCase}
     */
    private IncidentCase readCase(String line) {
        try {
            // 未知字段已被 ObjectMapper 配置为忽略，便于数据集后续扩展而不影响加载
            return objectMapper.readValue(line, IncidentCase.class);
        } catch (JsonProcessingException exception) {
            // 转成运行期异常，并把原始行带进消息，方便定位是哪一条数据有问题
            throw new IllegalStateException("Invalid incident JSON: " + line, exception);
        }
    }

    /**
     * 解析数据集文件的实际路径。
     *
     * 按“配置值 -> 模块目录 -> 仓库根目录”的顺序依次尝试，返回第一个存在的普通文件；
     * 全部不存在时抛出异常并列出所有已尝试的绝对路径。
     *
     * @return 存在且可读的数据集文件路径
     * @throws IllegalStateException 所有候选路径都不存在
     */
    private Path resolvePath() {
        // 候选路径：显式配置优先，其余覆盖从仓库根目录或 demo 目录启动的两种常见情况
        List<Path> candidates = new ArrayList<>();
        if (configuredPath != null && !configuredPath.isBlank()) {
            candidates.add(Path.of(configuredPath));
        }
        candidates.add(Path.of("demo-data", "incidents.jsonl"));
        candidates.add(Path.of("..", "demo-data", "incidents.jsonl"));

        // 仅用于组装失败信息，让启动报错能直接看出查过哪些位置
        List<Path> tried = new ArrayList<>();
        for (Path candidate : candidates) {
            // 先转绝对路径再判断，避免相对路径在不同工作目录下产生歧义
            Path absolute = candidate.toAbsolutePath().normalize();
            tried.add(absolute);
            // 找到第一个普通文件即返回，保持“配置优先”的查找顺序
            if (Files.isRegularFile(absolute)) {
                return absolute;
            }
        }

        // 全部候选都不存在时，把尝试过的路径一并抛出，便于排查工作目录或配置问题
        throw new IllegalStateException("Mock dataset not found. Tried: " + tried);
    }
}

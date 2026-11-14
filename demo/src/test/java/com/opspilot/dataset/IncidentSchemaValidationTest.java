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

/**
 * 数据集 JSON Schema 校验测试。
 */
class IncidentSchemaValidationTest {

    /** 待校验的 incidents.jsonl 数据集路径。 */
    private static final Path DATASET = DatasetPaths.incidents();

    /** 数据集对应的 JSON Schema 文件路径。 */
    private static final Path SCHEMA = DatasetPaths.schema();

    /** 用于把 JSONL 行解析成 JsonNode 的 ObjectMapper。 */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 逐行校验数据集，确保每一行都符合 JSON Schema。
     *
     * @throws Exception 读取文件或解析 JSON 失败
     */
    @Test
    void everyIncidentMatchesJsonSchema() throws Exception {
        // 使用 JSON Schema 2020-12 版本创建校验器
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(SCHEMA.toUri());

        // 读取所有非空行，每行代表一条故障用例
        List<String> lines = Files.readAllLines(DATASET, StandardCharsets.UTF_8)
                .stream()
                .filter(line -> !line.isBlank())
                .toList();

        for (String line : lines) {
            // 把单行文本解析成 JSON 树后交给 Schema 校验
            JsonNode incident = MAPPER.readTree(line);
            Set<ValidationMessage> errors = schema.validate(incident);
            // 校验失败时把 caseId 和具体错误一起输出，便于定位问题数据
            assertTrue(errors.isEmpty(),
                    () -> incident.path("caseId").asText() + " schema errors: " + errors);
        }
    }
}

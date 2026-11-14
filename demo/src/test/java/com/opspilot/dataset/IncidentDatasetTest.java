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

/**
 * 数据集完整性测试。
 */
class IncidentDatasetTest {

    /** 待校验的 incidents.jsonl 数据集路径。 */
    private static final Path DATASET = DatasetPaths.incidents();

    /** 支持 Java 时间类型的 JSONL 反序列化器。 */
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    /**
     * 校验数据集共有 50 条用例，且每个根因分类各 10 条。
     *
     * @throws Exception 读取数据集失败
     */
    @Test
    void datasetHasFiftyCasesAndTenPerCategory() throws Exception {
        // 加载全部用例
        List<IncidentCase> cases = load();

        // 总数必须为 50
        assertEquals(50, cases.size());

        // 按根因分类统计用例数量
        Map<String, Long> counts = cases.stream()
                .collect(Collectors.groupingBy(c -> c.category().name(), Collectors.counting()));

        // 数据集中计划覆盖的 5 个根因分类，每个都应有 10 条
        for (String category : List.of(
                "BAD_DEPLOY",
                "DEPENDENCY_TIMEOUT",
                "CONNECTION_POOL_EXHAUSTION",
                "TRAFFIC_SPIKE",
                "CONFIG_ERROR")) {
            assertEquals(10L, counts.getOrDefault(category, 0L), category);
        }
    }

    /**
     * 校验 caseId 和 alertId 在数据集内全局唯一。
     *
     * @throws Exception 读取数据集失败
     */
    @Test
    void caseIdsAndAlertIdsAreUnique() throws Exception {
        // 加载全部用例
        List<IncidentCase> cases = load();

        // 去重后的 caseId 数量应与用例总数一致
        assertEquals(cases.size(), cases.stream().map(IncidentCase::caseId).distinct().count());
        // 去重后的 alertId 数量也应与用例总数一致
        assertEquals(cases.size(), cases.stream().map(c -> c.alert().alertId()).distinct().count());
    }

    /**
     * 校验每条用例都具备告警、信号包和标准答案，且分类与标准答案一致。
     *
     * @throws Exception 读取数据集失败
     */
    @Test
    void everyCaseHasGroundTruthAndSignals() throws Exception {
        for (IncidentCase incident : load()) {
            // 基础对象不能缺失
            assertNotNull(incident.alert(), incident.caseId());
            assertNotNull(incident.signals(), incident.caseId());
            assertNotNull(incident.groundTruth(), incident.caseId());
            // 用例冗余的分类字段必须与标准答案保持一致
            assertEquals(incident.category(), incident.groundTruth().rootCauseCategory(), incident.caseId());

            // 指标、日志和期望证据类型不能为空，否则无法支撑调查与评测
            assertFalse(incident.signals().metrics().isEmpty(), incident.caseId());
            assertFalse(incident.signals().logs().isEmpty(), incident.caseId());
            assertFalse(incident.groundTruth().expectedEvidenceTypes().isEmpty(), incident.caseId());
            // 期望动作类型和风险等级必须显式标注
            assertNotNull(incident.groundTruth().expectedActionType(), incident.caseId());
            assertNotNull(incident.groundTruth().expectedActionRisk(), incident.caseId());
        }
    }

    /**
     * 校验每条用例至少包含发布记录或依赖信号之一。
     *
     * @throws Exception 读取数据集失败
     */
    @Test
    void everyCaseHasDeploymentOrDependencySignal() throws Exception {
        for (IncidentCase incident : load()) {
            // 故障根因通常能通过变更或依赖信号定位，二者至少要有其一
            assertTrue(
                    !incident.signals().deployments().isEmpty()
                            || !incident.signals().dependencies().isEmpty(),
                    incident.caseId()
            );
        }
    }

    /**
     * 从 JSONL 数据集加载全部故障用例。
     *
     * @return 故障用例列表
     * @throws Exception 文件不存在或反序列化失败
     */
    private List<IncidentCase> load() throws Exception {
        // 先确认数据集存在，避免报出难以理解的空列表错误
        assertTrue(Files.exists(DATASET), "Dataset not found: " + DATASET);

        List<IncidentCase> cases = new ArrayList<>();
        // 逐行解析非空文本，每行对应一条故障用例
        for (String line : Files.readAllLines(DATASET, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                cases.add(MAPPER.readValue(line, IncidentCase.class));
            }
        }
        return cases;
    }
}

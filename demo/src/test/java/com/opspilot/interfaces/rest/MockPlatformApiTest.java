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

/**
 * Mock 平台 API 集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class MockPlatformApiTest {

    /** 用于在不启动真实 HTTP 端口的情况下调用 Controller。 */
    @Autowired
    private MockMvc mockMvc;

    /** 用于在每个测试结束后清理激活状态。 */
    @Autowired
    private MockPlatformService mockPlatformService;

    /**
     * 每个测试结束后清除激活的故障，避免测试之间相互影响。
     */
    @AfterEach
    void resetFault() {
        // 重置服务端的激活状态
        mockPlatformService.reset();
    }

    /**
     * 校验用例列表返回 50 条，且不包含 groundTruth 字段。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void loadsFiftyCasesWithoutGroundTruth() throws Exception {
        mockMvc.perform(get("/mock/cases"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(50))
                .andExpect(jsonPath("$[0].caseId").value("CASE-001"))
                // 标准答案不能出现在对外响应中
                .andExpect(jsonPath("$[0].groundTruth").doesNotExist());
    }

    /**
     * 校验可以按告警 ID 查询告警。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void getsAlertById() throws Exception {
        // 使用数据集中的固定告警 ID 查询，断言返回对应服务和级别
        mockMvc.perform(get("/mock/alerts/ALERT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value("ALERT-001"))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.severity").value("P1"));
    }

    /**
     * 校验可以按服务和指标名查询指标。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void queriesMetricsByServiceAndName() throws Exception {
        // 指定服务和指标名，验证过滤条件生效
        mockMvc.perform(get("/mock/metrics")
                        .param("service", "order-service")
                        .param("name", "http.server.requests.error_rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].name").value("http.server.requests.error_rate"));
    }

    /**
     * 校验可以按级别查询错误日志。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void queriesErrorLogs() throws Exception {
        // 只查询 ERROR 级别日志，验证级别过滤生效
        mockMvc.perform(get("/mock/logs")
                        .param("service", "order-service")
                        .param("level", "ERROR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].level").value("ERROR"));
    }

    /**
     * 校验可以查询服务的发布记录。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void queriesDeployments() throws Exception {
        // 查询指定服务的发布记录，验证返回结果包含版本信息
        mockMvc.perform(get("/mock/deployments")
                        .param("service", "order-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].version").exists());
    }

    /**
     * 校验可以查询服务的依赖健康度。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void queriesDependencies() throws Exception {
        // 查询指定服务的依赖快照，验证返回结果包含被依赖服务
        mockMvc.perform(get("/mock/dependencies")
                        .param("service", "order-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].service").value("order-service"))
                .andExpect(jsonPath("$[0].dependsOn").exists());
    }

    /**
     * 校验激活故障后返回对应告警，并且可以查询到当前激活状态。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void activatesFaultAndReturnsItsAlert() throws Exception {
        // 激活 CASE-001，期望返回用例 ID 和对应告警
        mockMvc.perform(post("/mock/faults/CASE-001/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value("CASE-001"))
                .andExpect(jsonPath("$.alert.alertId").value("ALERT-001"));

        // 再查询当前激活故障，确认状态已保存
        mockMvc.perform(get("/mock/faults/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.caseId").value("CASE-001"));
    }

    /**
     * 校验重置接口会清除当前激活的故障。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void resetsActiveFault() throws Exception {
        // 先激活一条故障
        mockMvc.perform(post("/mock/faults/CASE-001/activate"))
                .andExpect(status().isOk());

        // 调用重置接口，固定返回 204
        mockMvc.perform(post("/mock/faults/reset"))
                .andExpect(status().isNoContent());

        // 重置后查询当前故障应返回 204
        mockMvc.perform(get("/mock/faults/active"))
                .andExpect(status().isNoContent());
    }

    /**
     * 校验查询不存在的告警返回 404。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void unknownAlertReturns404() throws Exception {
        // 使用不存在的告警 ID，期望统一错误处理器返回 404
        mockMvc.perform(get("/mock/alerts/ALERT-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    /**
     * 校验起止时间颠倒时返回 400。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void invalidTimeRangeReturns400() throws Exception {
        // from 晚于 to，期望应用服务校验后返回 400
        mockMvc.perform(get("/mock/metrics")
                        .param("service", "order-service")
                        .param("from", "2026-11-11T00:00:00Z")
                        .param("to", "2026-11-10T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    /**
     * 校验缺少必填参数 service 时返回 400。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void missingServiceReturns400() throws Exception {
        // 不传必填参数 service，期望缺少参数处理器返回 400
        mockMvc.perform(get("/mock/metrics"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Missing request parameter"));
    }
}

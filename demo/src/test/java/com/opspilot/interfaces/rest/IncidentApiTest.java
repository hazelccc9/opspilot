package com.opspilot.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 告警与故障 API 集成测试。
 */
@SpringBootTest
@AutoConfigureMockMvc
class IncidentApiTest {

    /** 用于在不启动真实 HTTP 端口的情况下调用 Controller。 */
    @Autowired
    private MockMvc mockMvc;

    /**
     * 校验创建故障后可以通过 Location 头读回同一故障。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void createsIncidentAndReadsItBack() throws Exception {
        // 使用随机 alertId，避免与内存仓库中的历史数据冲突
        String alertId = "ALERT-IT-" + UUID.randomUUID();
        String body = alertBody(alertId, "order-service");

        // 提交告警，期望返回 201、Location 头以及 NEW 状态
        MvcResult result = mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.alertId").value(alertId))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andReturn();

        // 取出 Location 头，作为后续查询地址
        String location = result.getResponse().getHeader("Location");
        assertNotNull(location);

        // 通过 Location 查询，确认读回的是同一条故障
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertId").value(alertId))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    /**
     * 校验同一 alertId 重复创建故障时返回 409。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void duplicateAlertReturns409() throws Exception {
        // 准备一个不会与历史数据重复的告警
        String alertId = "ALERT-IT-DUP-" + UUID.randomUUID();
        String body = alertBody(alertId, "payment-service");

        // 第一次创建应成功
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // 第二次提交同一告警应被幂等校验拦截
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate alert"));
    }

    /**
     * 校验缺少必填字段时返回 400 并给出字段级错误。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void invalidRequestReturns400WithFieldErrors() throws Exception {
        // 故意省略 service 字段，触发 Bean Validation
        String body = """
                {
                  "alertId": "ALERT-IT-INVALID",
                  "symptom": "5xx spike",
                  "severity": "P1",
                  "firedAt": "2026-11-10T08:00:00Z",
                  "source": "prometheus",
                  "description": "invalid request"
                }
                """;

        // 期望 400，并且 errors 中包含缺失的 service 字段
        mockMvc.perform(post("/api/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.service").exists());
    }

    /**
     * 校验查询不存在的故障返回 404。
     *
     * @throws Exception MockMvc 调用失败
     */
    @Test
    void unknownIncidentReturns404() throws Exception {
        // 使用不存在的故障 ID 查询
        mockMvc.perform(get("/api/incidents/INC-UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    /**
     * 构造用于创建告警的 JSON 请求体。
     *
     * @param alertId 告警 ID
     * @param service 服务名
     * @return JSON 字符串
     */
    private static String alertBody(String alertId, String service) {
        // 使用文本块保持 JSON 可读性，并注入调用方传入的字段
        return """
                {
                  "alertId": "%s",
                  "service": "%s",
                  "symptom": "HTTP 5xx rate > 10%% for 5m",
                  "severity": "P1",
                  "firedAt": "2026-11-10T08:00:00Z",
                  "source": "prometheus",
                  "description": "5xx rate 12%%",
                  "labels": { "env": "prod" }
                }
                """.formatted(alertId, service);
    }
}

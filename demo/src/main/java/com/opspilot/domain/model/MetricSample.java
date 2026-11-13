package com.opspilot.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * 单个指标采样点。
 *
 * <p>它不是完整的时间序列，而是一个“在某个时间点、某个服务、某组标签下的值”。
 * 评测数据集用多个采样点表达趋势；真实运行时，Agent 通过指标工具查询时间窗，
 * 而不是直接读取这个对象。
 */
public record MetricSample(
        /* 指标名，例如 http.server.requests.error_rate、jvm.cpu.usage、db.connection.pending */
        String name,
        /* 指标所属服务 */
        String service,
        /* 采样时间（UTC），不是告警触发时间 */
        Instant timestamp,
        /* 指标值 */
        double value,
        /* 单位，例如 ratio、ms、count、qps */
        String unit,
        /* 维度标签，例如 status=5xx、pool=primary；可能为空 Map，但不应为 null */
        Map<String, String> labels
) {
}

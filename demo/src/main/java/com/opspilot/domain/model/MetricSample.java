package com.opspilot.domain.model;

import java.time.Instant;
import java.util.Map;

/**
 * 单个指标采样点。
 *
 * <p>它不是完整的时间序列，而是一个“在某个时间点、某个服务、某组标签下的值”。
 * 评测数据集用多个采样点表达趋势；真实运行时，Agent 通过指标工具查询时间窗，
 * 而不是直接读取这个对象。
 *
 * @param name      指标名，例如 {@code http.server.requests.error_rate}、
 *                  {@code jvm.cpu.usage}、{@code db.connection.pending}
 * @param service   指标所属服务
 * @param timestamp 采样时间（UTC），不是告警触发时间
 * @param value     指标值
 * @param unit      单位，例如 {@code ratio}、{@code ms}、{@code count}、{@code qps}
 * @param labels    维度标签，例如 {@code status=5xx}、{@code pool=primary}；
 *                  可能为空 Map，但不应为 {@code null}
 */
public record MetricSample(
        String name,
        String service,
        Instant timestamp,
        double value,
        String unit,
        Map<String, String> labels
) {
}

package com.opspilot.domain.model;

import java.util.List;

/**
 * 一条合成故障用例的原始信号包：发布、指标、日志、依赖。
 *
 * <p>这是数据集为了“离线、可重复评测”而提供的整包快照。真实运行时，Agent 不会一次性
 * 拿到所有信号，而是通过只读工具按需查询；工具需要自己处理时间窗、过滤条件和分页。
 *
 * <p>四个列表都允许为空（例如没有发布记录），但不应为 {@code null}。
 * 数据只包含合成内容，不包含真实生产数据。
 *
 * @param deployments  相关发布/配置变更记录
 * @param metrics      相关指标采样点
 * @param logs         相关日志事件
 * @param dependencies 相关依赖健康度快照
 */
public record IncidentSignals(
        List<Deployment> deployments,
        List<MetricSample> metrics,
        List<LogEvent> logs,
        List<DependencyStatus> dependencies
) {
}

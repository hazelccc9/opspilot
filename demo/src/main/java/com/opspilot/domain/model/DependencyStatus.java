package com.opspilot.domain.model;

import com.opspilot.domain.enums.DependencyHealth;

/**
 * 调用方视角的下游依赖健康度快照。
 *
 * 例如 {@code order-service -> payment-service}：{@code service} 是调用方
 * {@code order-service}，{@code dependsOn} 是被依赖方 {@code payment-service}，
 * {@code status/latencyP95Ms/errorRate} 描述的是 {@code payment-service} 的健康状况。
 */
public record DependencyStatus(
        /* 调用方服务名 */
        String service,
        /* 被依赖的服务名 */
        String dependsOn,
        /* 依赖健康状态，见 DependencyHealth */
        DependencyHealth status,
        /* 下游调用的 P95 延迟，单位毫秒 */
        double latencyP95Ms,
        /* 下游调用的错误率，取值 0.0 ~ 1.0 */
        double errorRate
) {
}

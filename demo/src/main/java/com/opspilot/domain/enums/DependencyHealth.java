package com.opspilot.domain.enums;

/**
 * 下游依赖的健康状态。
 *
 * <p>它描述的是“被依赖方”的状态，不是调用方自身的状态。例如
 * {@code order-service -> payment-service} 的记录中，{@code service} 是
 * {@code order-service}，{@code dependsOn} 是 {@code payment-service}，
 * 健康状态描述的是 {@code payment-service}。
 */
public enum DependencyHealth {

    /** 依赖正常：延迟和错误率都在基线范围内。 */
    UP,

    /** 依赖降级：延迟升高或错误率上升，但尚未完全不可用。 */
    DEGRADED,

    /** 依赖不可用：大量超时、连接失败或熔断打开。 */
    DOWN
}

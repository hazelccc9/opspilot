package com.opspilot.domain.enums;

/**
 * Agent 可以建议的处置动作类型。
 *
 * <p>除 {@link #ESCALATE} 和 {@link #NO_OP} 外，其余动作都会改变生产状态，属于写操作：
 * 必须设置 {@code Action.requiresApproval = true}，并且默认只允许 dry-run。
 * 本项目不会连接真实生产环境执行这些动作。
 */
public enum ActionType {

    /** 回滚到上一个稳定版本。适用于坏发布、配置回滚等场景。 */
    ROLLBACK,

    /** 重启实例或服务。适用于连接泄漏、进程假死等需要快速恢复的场景。 */
    RESTART,

    /** 扩容（增加副本数或资源）。适用于流量突增、容量不足。 */
    SCALE_UP,

    /** 扩大数据库连接池。适用于连接池耗尽，但需要同时排查慢查询/泄漏。 */
    INCREASE_POOL_SIZE,

    /** 对下游依赖启用熔断/降级，避免调用方被超时拖垮。 */
    ENABLE_CIRCUIT_BREAKER,

    /** 修正配置值并触发刷新。适用于配置错误且可以安全热修复的场景。 */
    FIX_CONFIG,

    /** 关闭某个功能开关，快速止血，保留后续修复时间。 */
    DISABLE_FEATURE_FLAG,

    /** 转交人工处理。不是写操作，但意味着 Agent 主动放弃自动处置。 */
    ESCALATE,

    /** 不做任何处置，仅记录结论。适用于误报或影响可忽略的告警。 */
    NO_OP
}

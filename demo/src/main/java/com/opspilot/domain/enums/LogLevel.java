package com.opspilot.domain.enums;

/**
 * 日志级别，与常见日志框架的语义保持一致。
 *
 * <p>Agent 调查时通常优先看 {@code WARN} 和 {@code ERROR}，但 {@code INFO} 中的
 * 启动、配置刷新、连接池初始化等信息也可能成为关键证据。
 */
public enum LogLevel {

    /** 调试信息，生产环境通常不输出或采样输出。 */
    DEBUG,

    /** 正常流程信息，例如启动完成、配置刷新、请求处理摘要。 */
    INFO,

    /** 警告：暂时不影响主流程，但可能是故障前兆。 */
    WARN,

    /** 错误：请求失败、异常、超时、资源耗尽等，需要重点调查。 */
    ERROR
}

package com.opspilot.domain.model;

import com.opspilot.domain.enums.LogLevel;

import java.time.Instant;
import java.util.Map;

/**
 * 结构化日志事件
 *
 * {@code message} 是给人看的文本，{@code attributes} 是给程序过滤/聚合的结构化字段。
 * 生产环境中日志可能包含 PII，因此进入 Agent 上下文前必须先脱敏；本项目只使用合成数据。
 */
public record LogEvent(
        /* 日志唯一 ID */
        String logId,
        /* 日志所属服务 */
        String service,
        /* 日志时间（UTC） */
        Instant timestamp,
        /* 日志级别，见 LogLevel */
        LogLevel level,
        /* 日志正文，例如异常信息、超时提示、连接池耗尽提示 */
        String message,
        /* 链路追踪 ID，用于把同一请求的日志串起来；没有时为 null */
        String traceId,
        /* 结构化字段，例如 dependency=payment-service、pool=HikariPool-1；可能为空 Map，但不应为 null */
        Map<String, String> attributes
) {
}

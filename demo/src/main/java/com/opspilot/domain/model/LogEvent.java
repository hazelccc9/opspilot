package com.opspilot.domain.model;

import com.opspilot.domain.enums.LogLevel;

import java.time.Instant;
import java.util.Map;

public record LogEvent(
        String logId,
        String service,
        Instant timestamp,
        LogLevel level,
        String message,
        String traceId,
        Map<String, String> attributes
) {
}

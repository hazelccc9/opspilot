package com.opspilot.domain.enums;

public enum ActionType {
    ROLLBACK,
    RESTART,
    SCALE_UP,
    INCREASE_POOL_SIZE,
    ENABLE_CIRCUIT_BREAKER,
    FIX_CONFIG,
    DISABLE_FEATURE_FLAG,
    ESCALATE,
    NO_OP
}

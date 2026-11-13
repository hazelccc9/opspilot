package com.opspilot.domain.enums;

/**
 * 证据类型：Agent 在调查中收集到的信息类别。
 *
 * <p>评测时会把 Agent 实际收集到的类型集合与 {@code GroundTruth.expectedEvidenceTypes}
 * 比较，计算 Evidence Recall / Precision。
 */
public enum EvidenceType {

    /** 指标采样，例如错误率、P95 延迟、CPU、连接池使用量。 */
    METRIC,

    /** 日志事件，例如异常堆栈、超时、连接池耗尽、配置绑定失败。 */
    LOG,

    /** 发布记录，例如版本、提交、变更内容、发布时间。 */
    DEPLOYMENT,

    /** 依赖健康度，例如下游服务的延迟、错误率和 UP/DEGRADED/DOWN 状态。 */
    DEPENDENCY,

    /** 运维手册（Runbook）中的处置步骤或判断依据。 */
    RUNBOOK,

    /** 历史相似故障，用于参考根因和处置方式。 */
    SIMILAR_INCIDENT
}

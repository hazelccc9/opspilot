package com.opspilot.domain.enums;

/**
 * 发布记录的状态
 *
 * 注意：配置变更在本项目中也会生成一条 {@code Deployment} 记录（版本形如
 * {@code config-2026.11.10-1}），因此“发布”既可能是代码发布，也可能是配置变更。
 * Agent 需要结合变更内容、日志和指标来区分 {@code BAD_DEPLOY} 与 {@code CONFIG_ERROR}。
 */
public enum DeploymentStatus {

    /** 发布成功完成。 */
    SUCCESS,

    /** 发布失败，可能未完成或启动失败。 */
    FAILED,

    /** 发布后已回滚到上一版本。 */
    ROLLED_BACK
}

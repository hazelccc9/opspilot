package com.opspilot.domain.model;

import com.opspilot.domain.enums.DeploymentStatus;

import java.time.Instant;
import java.util.List;

/**
 * 一次代码发布或配置变更记录。
 *
 * 本项目中，配置变更也会生成一条 {@code Deployment}（版本形如
 * {@code config-2026.11.10-1}），以便 Agent 用同一套“变更时间线”能力来关联
 * {@code BAD_DEPLOY} 和 {@code CONFIG_ERROR}。二者的区别主要靠变更内容、日志和指标判断。
 */
public record Deployment(
        /* 发布唯一 ID */
        String deploymentId,
        /* 发布目标服务 */
        String service,
        /* 版本号；代码发布如 v1.8.0，配置变更如 config-2026.11.10-1 */
        String version,
        /* 发布时间（UTC） */
        Instant deployedAt,
        /* 发布人/系统，例如 ci-bot */
        String deployedBy,
        /* 代码提交 SHA；配置变更可为空字符串或配置仓库的提交号 */
        String commitSha,
        /* 发布状态，见 DeploymentStatus */
        DeploymentStatus status,
        /* 变更摘要列表，例如“修改支付超时时间”“升级 HTTP 客户端” */
        List<String> changes
) {
}

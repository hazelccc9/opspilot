package com.opspilot.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * 运维手册（Runbook），也是 RAG 知识库的主要来源。
 *
 * <p>Runbook 应该描述“在什么症状下、按什么顺序、做哪些检查和处置”，
 * 而不是只写一句“重启服务”。Agent 检索到片段后，必须通过 {@link Citation}
 * 标明文档 ID、版本和原文，避免把模型常识伪装成公司规范。
 */
public record Runbook(
        /* 手册唯一 ID */
        String runbookId,
        /* 手册标题 */
        String title,
        /* 适用服务；跨服务/全局手册可以使用 all */
        String service,
        /* 适用症状，例如 HTTP 5xx rate > 10% */
        String symptom,
        /* 手册版本，例如 v3 或发布日期 */
        String version,
        /* 手册正文，通常为 Markdown */
        String content,
        /* 检索标签，例如 rollback、database、timeout */
        List<String> tags,
        /* 最后更新时间（UTC） */
        Instant updatedAt
) {
}

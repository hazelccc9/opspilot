package com.opspilot.domain.model;

/**
 * 证据或知识片段的可核对出处。
 *
 * <p>RAG 场景中，Agent 不能只说“Runbook 里写了要回滚”，还必须能给出
 * 文档 ID、切片 ID、版本和原文片段。这样人类才能验证结论不是模型编造的。
 */
public record Citation(
        /* 文档稳定 ID，例如 runbook-order-service-5xx */
        String docId,
        /* 文档切片 ID；同一文档重新切分后应能区分 */
        String chunkId,
        /* 文档标题 */
        String title,
        /* 文档版本，例如 v3 或发布日期 */
        String version,
        /* 命中的原文片段，用于人工快速核对 */
        String snippet,
        /* 可访问的文档链接或内部 URI；无法提供时为 null */
        String uri
) {
}

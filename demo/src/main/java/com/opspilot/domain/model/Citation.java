package com.opspilot.domain.model;

/**
 * 证据或知识片段的可核对出处。
 *
 * <p>RAG 场景中，Agent 不能只说“Runbook 里写了要回滚”，还必须能给出
 * 文档 ID、切片 ID、版本和原文片段。这样人类才能验证结论不是模型编造的。
 *
 * @param docId   文档稳定 ID，例如 {@code runbook-order-service-5xx}
 * @param chunkId 文档切片 ID；同一文档重新切分后应能区分
 * @param title   文档标题
 * @param version 文档版本，例如 {@code v3} 或发布日期
 * @param snippet 命中的原文片段，用于人工快速核对
 * @param uri     可访问的文档链接或内部 URI；无法提供时为 {@code null}
 */
public record Citation(
        String docId,
        String chunkId,
        String title,
        String version,
        String snippet,
        String uri
) {
}

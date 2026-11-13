package com.opspilot.domain.model;

public record Citation(
        String docId,
        String chunkId,
        String title,
        String version,
        String snippet,
        String uri
) {
}

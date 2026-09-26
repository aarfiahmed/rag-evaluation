package com.example.rageval.domain;

import java.util.Map;

public record Chunk(String id, String documentId, String text, String title, String section, Integer pageNumber,
                    Map<String, String> metadata) {
    public Chunk {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}

package com.example.rageval.domain;

import java.util.List;

public record RagResponse(String question, List<RetrievedChunk> retrievedChunks, String answer) {
    public RagResponse {
        retrievedChunks = List.copyOf(retrievedChunks);
    }
}

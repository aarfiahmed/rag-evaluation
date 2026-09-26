package com.example.rageval.domain;

import java.util.List;
import java.util.Map;

public record EvalCase(String id, String question, List<String> expectedDocumentIds, String expectedAnswer,
                       List<String> acceptableAnswers, Map<String, String> metadata) {
    public EvalCase {
        expectedDocumentIds = List.copyOf(expectedDocumentIds);
        acceptableAnswers = acceptableAnswers == null ? List.of() : List.copyOf(acceptableAnswers);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}

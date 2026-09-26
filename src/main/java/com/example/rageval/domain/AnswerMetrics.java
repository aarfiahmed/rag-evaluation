package com.example.rageval.domain;

public record AnswerMetrics(boolean exactMatch, double lexicalSimilarity, boolean groundedOnRetrievedContext,
                            double judgeScore, String judgeReason) {
}

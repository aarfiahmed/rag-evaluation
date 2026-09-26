package com.example.rageval.domain;

public record RetrievalMetrics(double recallAtK, double precisionAtK, double meanReciprocalRank, double ndcgAtK) {
}

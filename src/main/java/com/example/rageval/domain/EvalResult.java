package com.example.rageval.domain;

public record EvalResult(String caseId, RetrievalMetrics retrieval, AnswerMetrics answer, boolean endToEndPass) {
}

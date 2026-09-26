package com.example.rageval.domain;

import java.util.List;

public record EvalReport(int totalCases, int passedCases, double passRate, double averageRecallAtK,
                         double averagePrecisionAtK, double averageMrr, double averageNdcg,
                         double averageLexicalSimilarity, double averageJudgeScore, List<EvalResult> results) {
}

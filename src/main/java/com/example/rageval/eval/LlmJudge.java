package com.example.rageval.eval;

import com.example.rageval.domain.RetrievedChunk;

import java.util.List;

public interface LlmJudge {
    JudgeResult evaluate(String q, String reference, String actual, List<RetrievedChunk> context);

    record JudgeResult(double score, String reason) {
        static JudgeResult disabled() {
            return new JudgeResult(0, "LLM judge disabled");
        }
    }
}

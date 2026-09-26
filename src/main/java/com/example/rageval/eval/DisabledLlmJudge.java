package com.example.rageval.eval;

import com.example.rageval.domain.RetrievedChunk;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnMissingBean(LlmJudge.class)
public class DisabledLlmJudge implements LlmJudge {
    public JudgeResult evaluate(String q, String r, String a, List<RetrievedChunk> c) {
        return JudgeResult.disabled();
    }
}

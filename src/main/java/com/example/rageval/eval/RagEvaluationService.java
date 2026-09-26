package com.example.rageval.eval;

import com.example.rageval.domain.*;
import com.example.rageval.rag.RagService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RagEvaluationService {
    private final RagService rag;
    private final RetrievalEvaluator retrieval;
    private final AnswerEvaluator answer;
    private final LlmJudge judge;

    public RagEvaluationService(RagService r, RetrievalEvaluator re, AnswerEvaluator a, LlmJudge j) {
        rag = r;
        retrieval = re;
        answer = a;
        judge = j;
    }

    public EvalResult evaluateCase(EvalCase c, int k) {
        RagResponse rr = rag.ask(c.question(), k);
        RetrievalMetrics rm = retrieval.evaluate(rr.retrievedChunks(), c.expectedDocumentIds(), k);
        LlmJudge.JudgeResult jr = judge.evaluate(c.question(), c.expectedAnswer(), rr.answer(), rr.retrievedChunks());
        AnswerMetrics am = answer.evaluate(rr.answer(), c.expectedAnswer(), c.acceptableAnswers(), rr.retrievedChunks(), jr.score(), jr.reason());
        boolean retrievalPass = rm.recallAtK() >= 1;
        boolean answerPass = am.exactMatch() || am.lexicalSimilarity() >= .70 || (am.judgeScore() >= 4 && am.groundedOnRetrievedContext());
        return new EvalResult(c.id(), rm, am, retrievalPass && answerPass);
    }

    public EvalReport evaluateAll(List<EvalCase> cases, int k) {
        List<EvalResult> rs = cases.stream().map(c -> evaluateCase(c, k)).toList();
        int total = rs.size(), passed = (int) rs.stream().filter(EvalResult::endToEndPass).count();
        return new EvalReport(total, passed, total == 0 ? 0 : (double) passed / total, avg(rs, x -> x.retrieval().recallAtK()), avg(rs, x -> x.retrieval().precisionAtK()), avg(rs, x -> x.retrieval().meanReciprocalRank()), avg(rs, x -> x.retrieval().ndcgAtK()), avg(rs, x -> x.answer().lexicalSimilarity()), avg(rs, x -> x.answer().judgeScore()), rs);
    }

    private double avg(List<EvalResult> r, java.util.function.ToDoubleFunction<EvalResult> f) {
        return r.stream().mapToDouble(f).average().orElse(0);
    }
}

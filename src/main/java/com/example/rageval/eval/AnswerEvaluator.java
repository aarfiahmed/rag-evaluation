package com.example.rageval.eval;

import com.example.rageval.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AnswerEvaluator {
    public AnswerMetrics evaluate(String actual, String expected, List<String> acceptable, List<RetrievedChunk> retrieved, double judge, String reason) {
        String a = norm(actual);
        boolean exact = expected != null && a.equals(norm(expected));
        double sim = expected == null ? 0 : TextSimilarity.jaccard(actual, expected);
        for (String x : acceptable == null ? List.<String>of() : acceptable)
            sim = Math.max(sim, TextSimilarity.jaccard(actual, x));
        String ctx = retrieved.stream().map(x -> x.chunk().text().toLowerCase(Locale.ROOT)).reduce("", (x, y) -> x + " " + y);
        boolean grounded = actual != null && !actual.isBlank() && Arrays.stream(norm(actual).split("\\s+")).filter(w -> w.length() > 3).limit(6).anyMatch(ctx::contains);
        return new AnswerMetrics(exact, sim, grounded, judge, reason);
    }

    private String norm(String x) {
        return x == null ? "" : x.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}

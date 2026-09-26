package com.example.rageval.eval;

import com.example.rageval.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RetrievalEvaluator {
    public RetrievalMetrics evaluate(List<RetrievedChunk> retrieved, List<String> expected, int k) {
        Set<String> rel = new HashSet<>(expected);
        List<RetrievedChunk> top = retrieved.stream().limit(k).toList();
        long hit = top.stream().map(x -> x.chunk().documentId()).filter(rel::contains).distinct().count();
        double recall = rel.isEmpty() ? 0 : (double) hit / rel.size();
        double precision = top.isEmpty() ? 0 : (double) hit / top.size();
        double mrr = 0;
        for (RetrievedChunk x : top) {
            if (rel.contains(x.chunk().documentId())) {
                mrr = 1.0 / x.rank();
                break;
            }
        }
        double dcg = 0;
        for (int i = 0; i < top.size(); i++) {
            int r = rel.contains(top.get(i).chunk().documentId()) ? 1 : 0;
            dcg += r / (Math.log(i + 2) / Math.log(2));
        }
        int ideal = Math.min(rel.size(), k);
        double idcg = 0;
        for (int i = 0; i < ideal; i++) idcg += 1 / (Math.log(i + 2) / Math.log(2));
        return new RetrievalMetrics(recall, precision, mrr, idcg == 0 ? 0 : dcg / idcg);
    }
}

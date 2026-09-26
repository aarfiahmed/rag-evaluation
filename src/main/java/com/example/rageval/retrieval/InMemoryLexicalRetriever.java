package com.example.rageval.retrieval;

import com.example.rageval.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryLexicalRetriever implements Retriever {
    private final Map<String, Chunk> chunks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> tf = new ConcurrentHashMap<>();
    private final Map<String, Integer> df = new ConcurrentHashMap<>();

    public InMemoryLexicalRetriever() {
        replaceCorpus(List.of(
                new Chunk("leave-001", "employee-handbook", "Employees receive 20 annual leave days per calendar year. Unused leave may be carried forward up to 5 days to the next calendar year.", "Employee Handbook", "Leave Policy", 12, Map.of("type", "policy")),
                new Chunk("remote-001", "employee-handbook", "Employees may work remotely up to 3 days per week, subject to manager approval and team requirements.", "Employee Handbook", "Remote Work Policy", 14, Map.of("type", "policy")),
                new Chunk("insurance-001", "employee-handbook", "Eligible employees receive medical insurance coverage according to the benefits plan.", "Employee Handbook", "Insurance", 20, Map.of("type", "policy")),
                new Chunk("expense-001", "finance-policy", "Business expenses must be submitted within 30 days with valid receipts and manager approval.", "Finance Policy", "Expense Claims", 4, Map.of("type", "policy")),
                new Chunk("notice-001", "employee-handbook", "Employees should submit planned leave requests at least 3 working days before the leave starts.", "Employee Handbook", "Leave Policy", 13, Map.of("type", "policy"))));
    }

    public synchronized void replaceCorpus(Collection<Chunk> input) {
        chunks.clear();
        tf.clear();
        df.clear();
        for (Chunk c : input) {
            chunks.put(c.id(), c);
            List<String> terms = tokens(c.text());
            Map<String, Integer> f = new HashMap<>();
            terms.forEach(t -> f.merge(t, 1, Integer::sum));
            tf.put(c.id(), f);
            new HashSet<>(terms).forEach(t -> df.merge(t, 1, Integer::sum));
        }
    }

    @Override
    public List<RetrievedChunk> retrieve(String query, int topK) {
        if (query == null || query.isBlank()) return List.of();
        List<String> q = tokens(query);
        int n = chunks.size();
        double avg = tf.values().stream().mapToInt(m -> m.values().stream().mapToInt(Integer::intValue).sum()).average().orElse(1);
        final double k1 = 1.2, b = .75;
        List<Scored> scored = chunks.values().stream().map(c -> {
            Map<String, Integer> f = tf.get(c.id());
            int len = f.values().stream().mapToInt(Integer::intValue).sum();
            double s = 0;
            for (String term : q) {
                int freq = f.getOrDefault(term, 0);
                if (freq == 0) continue;
                int d = df.getOrDefault(term, 0);
                double idf = Math.log(1 + (n - d + .5) / (d + .5));
                s += idf * (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * len / avg));
            }
            return new Scored(c, s);
        }).filter(x -> x.score > 0).sorted(Comparator.comparingDouble(Scored::score).reversed().thenComparing(x -> x.chunk.id())).limit(topK).toList();
        List<RetrievedChunk> out = new ArrayList<>();
        int rank = 1;
        for (Scored s : scored) out.add(new RetrievedChunk(s.chunk, s.score, rank++));
        return out;
    }

    private List<String> tokens(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().split("\\s+")).filter(s -> s.length() > 1).toList();
    }

    private record Scored(Chunk chunk, double score) {
    }
}

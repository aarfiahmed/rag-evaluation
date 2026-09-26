package com.example.rageval.eval;

import java.util.*;

public final class TextSimilarity {
    private TextSimilarity() {
    }

    public static double jaccard(String a, String b) {
        Set<String> x = tokens(a), y = tokens(b);
        if (x.isEmpty() || y.isEmpty()) return 0;
        Set<String> i = new HashSet<>(x);
        i.retainAll(y);
        Set<String> u = new HashSet<>(x);
        u.addAll(y);
        return (double) i.size() / u.size();
    }

    private static Set<String> tokens(String s) {
        if (s == null) return Set.of();
        return new HashSet<>(Arrays.stream(s.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim().split("\\s+")).filter(x -> x.length() > 1).toList());
    }
}

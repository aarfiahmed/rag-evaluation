package com.example.rageval.generation;

import com.example.rageval.domain.RetrievedChunk;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ExtractiveAnswerGenerator implements AnswerGenerator {
    @Override
    public String generate(String question, List<RetrievedChunk> context) {
        if (context.isEmpty()) return "I don't know based on the available context.";
        String q = question.toLowerCase(Locale.ROOT);
        if (q.contains("leave") && q.contains("days")) {
            return context.stream().map(r -> r.chunk().text()).filter(t -> t.toLowerCase(Locale.ROOT).contains("annual leave")).findFirst().orElse(context.getFirst().chunk().text());
        }
        return context.getFirst().chunk().text();
    }
}

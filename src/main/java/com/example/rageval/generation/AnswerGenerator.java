package com.example.rageval.generation;

import com.example.rageval.domain.RetrievedChunk;

import java.util.List;

public interface AnswerGenerator {
    String generate(String question, List<RetrievedChunk> context);
}

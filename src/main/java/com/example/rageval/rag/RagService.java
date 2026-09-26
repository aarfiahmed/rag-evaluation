package com.example.rageval.rag;

import com.example.rageval.domain.*;
import com.example.rageval.generation.AnswerGenerator;
import com.example.rageval.retrieval.Retriever;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RagService {
    private final Retriever retriever;
    private final AnswerGenerator generator;

    public RagService(Retriever r, AnswerGenerator g) {
        retriever = r;
        generator = g;
    }

    public RagResponse ask(String q, int k) {
        List<RetrievedChunk> c = retriever.retrieve(q, k);
        return new RagResponse(q, c, generator.generate(q, c));
    }
}

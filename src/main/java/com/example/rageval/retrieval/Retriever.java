package com.example.rageval.retrieval;

import com.example.rageval.domain.RetrievedChunk;

import java.util.List;

public interface Retriever {
    List<RetrievedChunk> retrieve(String query, int topK);
}

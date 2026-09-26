package com.example.rageval;
import com.example.rageval.retrieval.InMemoryLexicalRetriever; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class InMemoryLexicalRetrieverTest {@Test void retrieves(){var r=new InMemoryLexicalRetriever().retrieve("How many annual leave days?",3);assertFalse(r.isEmpty());assertEquals("employee-handbook",r.getFirst().chunk().documentId());}}

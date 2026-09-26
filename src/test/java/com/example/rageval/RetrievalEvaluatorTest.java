package com.example.rageval;
import com.example.rageval.domain.*; import com.example.rageval.eval.RetrievalEvaluator; import org.junit.jupiter.api.Test; import java.util.*; import static org.junit.jupiter.api.Assertions.*;
class RetrievalEvaluatorTest {@Test void perfect(){Chunk c=new Chunk("c1","doc-1","relevant","t","s",1,Map.of());RetrievalMetrics m=new RetrievalEvaluator().evaluate(List.of(new RetrievedChunk(c,1,1)),List.of("doc-1"),1);assertEquals(1,m.recallAtK());assertEquals(1,m.precisionAtK());assertEquals(1,m.meanReciprocalRank());assertEquals(1,m.ndcgAtK());}}

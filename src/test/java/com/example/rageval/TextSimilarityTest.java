package com.example.rageval;
import com.example.rageval.eval.TextSimilarity; import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class TextSimilarityTest {@Test void identical(){assertEquals(1,TextSimilarity.jaccard("Employees receive 20 days","Employees receive 20 days"));}@Test void disjoint(){assertEquals(0,TextSimilarity.jaccard("Kafka partitions","Medical insurance"));}}

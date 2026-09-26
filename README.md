# RAG Evaluation — Spring Boot Reference Project

## 1. Purpose

This project evaluates a **RAG application**, not just an LLM. It separates retrieval quality, answer quality, and end-to-end behavior so a failed evaluation can be investigated instead of reduced to one opaque score.

```text
Question -> Retriever -> Retrieved Context -> Answer Generator -> Final Answer
                  \_______________________________________________/
                                  Evaluation
```

## 2. What is included

- `Retriever` abstraction and a zero-dependency BM25-like in-memory reference retriever.
- `AnswerGenerator` abstraction with a deterministic local generator and an optional OpenAI-compatible generator.
- Retrieval metrics: Recall@K, Precision@K, MRR, NDCG@K.
- Answer metrics: exact match, lightweight lexical similarity, grounding signal, optional LLM judge.
- End-to-end pass/fail and aggregate reports.
- Versionable JSON evaluation dataset.
- REST endpoints for RAG queries and evaluation runs.
- Unit tests.
- Optional LLM-as-a-judge using an OpenAI-compatible `/chat/completions` endpoint and JSON output.

## 3. Why RAG evaluation is different from LLM evaluation

A RAG failure can happen in retrieval or generation:

```text
Question -> Retriever -> WRONG context -> LLM -> WRONG answer
```

or:

```text
Question -> Retriever -> CORRECT context -> LLM -> WRONG answer
```

Therefore evaluate both stages separately and also evaluate the whole pipeline.

## 4. Evaluation dataset

Each case contains:

| Field | Purpose |
|---|---|
| `id` | Stable test case ID |
| `question` | User query |
| `expectedDocumentIds` | Ground-truth relevant document IDs |
| `expectedAnswer` | Reference answer |
| `acceptableAnswers` | Alternative valid answers |
| `metadata` | Reporting/filtering attributes |

Keep the dataset version-controlled in a real project.

## 5. Retrieval evaluation

### Recall@K
Measures whether relevant documents were retrieved in the top K.

`recall@K = relevant retrieved / total relevant`

### Precision@K
Measures how much of the top K is relevant.

`precision@K = relevant retrieved / K`

### MRR
Measures how early the first relevant result appears. A relevant result at rank 1 has reciprocal rank 1; rank 5 has 1/5.

### NDCG@K
Measures ranking quality. This reference implementation uses binary relevance; a production system can use graded relevance.

## 6. Answer evaluation

### Exact match
Useful for deterministic outputs but not sufficient for natural language.

### Lexical similarity
A cheap local signal based on token overlap. It is not semantic understanding and should not be the only answer metric.

### Grounding signal
Checks whether the answer has textual support in retrieved context. Treat this as a signal, not proof of factual correctness.

### LLM-as-a-judge
Optional judge receives the question, reference answer, retrieved context, and actual answer and returns a 0–5 score plus reason. Use it for semantic evaluation where deterministic metrics are insufficient.

## 7. End-to-end evaluation

A test case passes when retrieval and answer checks pass. The report preserves both sets of metrics so you can diagnose failures.

Example conceptual result:

```json
{
  "caseId": "EV-001",
  "retrieval": {"recallAtK": 1.0, "precisionAtK": 0.33, "meanReciprocalRank": 1.0, "ndcgAtK": 1.0},
  "answer": {"exactMatch": false, "lexicalSimilarity": 0.84, "groundedOnRetrievedContext": true, "judgeScore": 4.5},
  "endToEndPass": true
}
```

## 8. Architecture

```text
controller
  |-- RagController
  |-- EvalController

rag
  |-- RagService

retrieval
  |-- Retriever
  |-- InMemoryLexicalRetriever

generation
  |-- AnswerGenerator
  |-- ExtractiveAnswerGenerator
  |-- OpenAiCompatibleAnswerGenerator

eval
  |-- RagEvaluationService
  |-- RetrievalEvaluator
  |-- AnswerEvaluator
  |-- LlmJudge
  |-- OpenAiCompatibleLlmJudge
  |-- DisabledLlmJudge
  |-- TextSimilarity
```

The important production boundary is:

```text
Retriever          -> retrieval implementation
AnswerGenerator    -> model/provider implementation
LlmJudge           -> evaluation-provider implementation
EvaluationService  -> stable evaluation logic
```

This means a real Qdrant/Weaviate/OpenSearch retriever can replace the reference retriever without rewriting evaluation logic.

## 9. REST contract

### Query the RAG application

`POST /api/rag/query?topK=3`

```json
{"question":"How many annual leave days do employees receive?"}
```

The response includes the retrieved chunks and final answer. Keeping retrieved chunks in the response is important for debugging and evaluation.

### Evaluate one case

`POST /api/eval/case?topK=5`

```json
{
  "id":"EV-001",
  "question":"How many annual leave days do employees receive?",
  "expectedDocumentIds":["employee-handbook"],
  "expectedAnswer":"Employees receive 20 annual leave days."
}
```

### Evaluate a dataset

`POST /api/eval/run`

```json
{
  "topK": 5,
  "cases": [
    {
      "id":"EV-001",
      "question":"How many annual leave days do employees receive?",
      "expectedDocumentIds":["employee-handbook"],
      "expectedAnswer":"Employees receive 20 annual leave days."
    }
  ]
}
```

## 10. Production evolution

The local retriever exists so the reference project can run without a vector database. In a real RAG system replace it with a vector or hybrid retriever backed by Qdrant, Weaviate, OpenSearch, PostgreSQL/pgvector, etc.

A production pipeline typically becomes:

```text
Document ingestion
  -> chunking
  -> embeddings
  -> vector/hybrid index
  -> retriever
  -> optional reranker
  -> prompt builder
  -> LLM
  -> answer
  -> evaluation
```

The evaluation layer should remain independent from the storage/provider implementation.

## 11. CI/CD regression testing

A strong production pattern is:

```text
Code/prompt/model change
        |
        v
Run fixed evaluation dataset
        |
        +--> Retrieval metrics
        +--> Answer metrics
        +--> Optional judge
        |
        v
Compare with baseline
        |
     PASS / FAIL
```

Store with each run:

- dataset version
- prompt version
- model name/version
- embedding model
- chunking strategy
- retrieval configuration
- evaluation timestamp
- latency and token/cost metrics where available

Do not rely on a single overall score.

## 12. Important production caveats

- Lexical similarity is only a cheap signal.
- Grounding checks should be stronger for high-risk domains.
- LLM judges can make mistakes and should be calibrated with human-reviewed examples.
- Retrieval and generation metrics should remain separate.
- Thresholds should be tuned against your domain and failure costs, not copied blindly from examples.
- Evaluation datasets should contain difficult, ambiguous, boundary, and adversarial questions—not only easy happy paths.
- Never log sensitive retrieved content or user prompts by default; apply appropriate redaction and retention policies.

## 13. Key takeaway

The goal is not to prove that the LLM is good.

The goal is to answer:

> **Is my RAG application retrieving the right information and using that information to produce an acceptable answer?**

That requires measuring retrieval, generation, and end-to-end behavior separately.

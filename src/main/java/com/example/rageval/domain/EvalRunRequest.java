package com.example.rageval.domain;

import java.util.List;

public record EvalRunRequest(List<EvalCase> cases, Integer topK) {
}

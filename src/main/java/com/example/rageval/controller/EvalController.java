package com.example.rageval.controller;

import com.example.rageval.domain.*;
import com.example.rageval.eval.RagEvaluationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eval")
public class EvalController {
    private final RagEvaluationService eval;

    public EvalController(RagEvaluationService e) {
        eval = e;
    }

    @PostMapping("/case")
    public EvalResult one(@RequestBody EvalCase c, @RequestParam(defaultValue = "5") int topK) {
        validate(topK);
        return eval.evaluateCase(c, topK);
    }

    @PostMapping("/run")
    public EvalReport run(@RequestBody EvalRunRequest r) {
        int k = r.topK() == null ? 5 : r.topK();
        validate(k);
        if (r.cases() == null || r.cases().isEmpty()) throw new IllegalArgumentException("cases must not be empty");
        return eval.evaluateAll(r.cases(), k);
    }

    private void validate(int k) {
        if (k < 1 || k > 50) throw new IllegalArgumentException("topK must be between 1 and 50");
    }
}

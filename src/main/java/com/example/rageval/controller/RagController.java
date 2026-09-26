package com.example.rageval.controller;

import com.example.rageval.domain.*;
import com.example.rageval.rag.RagService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rag")
public class RagController {
    private final RagService rag;

    public RagController(RagService r) {
        rag = r;
    }

    @PostMapping("/query")
    public RagResponse query(@RequestBody RagQueryRequest req, @RequestParam(defaultValue = "3") int topK) {
        validate(topK);
        if (req.question() == null || req.question().isBlank())
            throw new IllegalArgumentException("question must not be blank");
        return rag.ask(req.question(), topK);
    }

    private void validate(int k) {
        if (k < 1 || k > 50) throw new IllegalArgumentException("topK must be between 1 and 50");
    }
}

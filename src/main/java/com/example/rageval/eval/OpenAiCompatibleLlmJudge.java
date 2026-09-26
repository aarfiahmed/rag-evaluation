package com.example.rageval.eval;

import com.example.rageval.domain.RetrievedChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

@Component
@ConditionalOnProperty(prefix = "rag.eval.llm-judge", name = "enabled", havingValue = "true")
public class OpenAiCompatibleLlmJudge implements LlmJudge {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String model;

    public OpenAiCompatibleLlmJudge(RestClient.Builder b, ObjectMapper m, @Value("${rag.eval.llm-judge.base-url}") String base, @Value("${rag.eval.llm-judge.api-key}") String key, @Value("${rag.eval.llm-judge.model}") String model) {
        client = b.baseUrl(base).defaultHeader("Authorization", "Bearer " + key).build();
        mapper = m;
        this.model = model;
    }

    @Override
    public JudgeResult evaluate(String q, String ref, String actual, List<RetrievedChunk> context) {
        String ctx = context.stream().map(x -> "[" + x.chunk().id() + "] " + x.chunk().text()).reduce("", (a, b) -> a + "\\n" + b);
        String p = "Evaluate a RAG answer. Score 0-5: 5 fully correct and grounded, 4 correct minor omission, 3 partial, 2 materially incomplete/unsupported, 1 mostly incorrect, 0 incorrect/unsupported. Return JSON {\\\"score\\\":number,\\\"reason\\\":\\\"short\\\"}.\\nQuestion: " + q + "\\nReference: " + ref + "\\nContext:\\n" + ctx + "\\nActual: " + actual;
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0);
        ArrayNode msgs = mapper.createArrayNode();
        msgs.add(mapper.createObjectNode().put("role", "system").put("content", "You are a strict RAG evaluator."));
        msgs.add(mapper.createObjectNode().put("role", "user").put("content", p));
        body.set("messages", msgs);
        body.set("response_format", mapper.createObjectNode().put("type", "json_object"));
        JsonNode r = client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
        String json = r == null ? null : r.path("choices").path(0).path("message").path("content").asText(null);
        if (json == null) throw new IllegalStateException("LLM judge returned no response");
        try {
            JsonNode n = mapper.readTree(json);
            return new JudgeResult(Math.max(0, Math.min(5, n.path("score").asDouble())), n.path("reason").asText(""));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid judge JSON", e);
        }
    }
}

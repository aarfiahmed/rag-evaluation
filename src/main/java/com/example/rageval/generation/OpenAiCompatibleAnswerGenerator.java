package com.example.rageval.generation;

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
@ConditionalOnProperty(prefix = "rag.llm", name = "enabled", havingValue = "true")
public class OpenAiCompatibleAnswerGenerator implements AnswerGenerator {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String model;

    public OpenAiCompatibleAnswerGenerator(RestClient.Builder b, ObjectMapper m, @Value("${rag.llm.base-url}") String base, @Value("${rag.llm.api-key}") String key, @Value("${rag.llm.model}") String model) {
        this.client = b.baseUrl(base).defaultHeader("Authorization", "Bearer " + key).build();
        this.mapper = m;
        this.model = model;
    }

    @Override
    public String generate(String question, List<RetrievedChunk> context) {
        String ctx = context.stream().map(r -> "[" + r.chunk().id() + "] " + r.chunk().text()).reduce("", (a, b) -> a + "\\n" + b);
        String prompt = "Answer using only the context. If unavailable say: I don't know based on the available context.\\nQuestion: " + question + "\\nContext:\\n" + ctx;
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("temperature", 0);
        ArrayNode msgs = mapper.createArrayNode();
        msgs.add(mapper.createObjectNode().put("role", "system").put("content", "You are a grounded RAG answer generator."));
        msgs.add(mapper.createObjectNode().put("role", "user").put("content", prompt));
        body.set("messages", msgs);
        JsonNode r = client.post().uri("/chat/completions").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
        String out = r == null ? null : r.path("choices").path(0).path("message").path("content").asText(null);
        if (out == null || out.isBlank()) throw new IllegalStateException("LLM returned no answer");
        return out;
    }
}

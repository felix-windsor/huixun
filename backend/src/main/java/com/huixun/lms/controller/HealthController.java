package com.huixun.lms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;
import com.huixun.lms.service.EmbeddingService;
import com.huixun.lms.service.OpenAiEmbeddingProvider;
import com.huixun.lms.service.QuestionGenerationService;
import com.huixun.lms.model.ContentFragment;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    private final EmbeddingService embeddingService;
    private final OpenAiEmbeddingProvider openAiProvider;
    private final QuestionGenerationService generationService;
    private final String providerName;

    public HealthController(EmbeddingService embeddingService,
                            OpenAiEmbeddingProvider openAiProvider,
                            QuestionGenerationService generationService,
                            @Value("${app.llm.provider}") String providerName) {
        this.embeddingService = embeddingService;
        this.openAiProvider = openAiProvider;
        this.generationService = generationService;
        this.providerName = providerName;
    }
    @GetMapping
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/embedding")
    public ResponseEntity<?> embeddingHealth() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("provider", providerName);
        try {
            if ("openai".equalsIgnoreCase(providerName)) {
                var v = openAiProvider.embed("health check");
                boolean ok = v != null && !v.isEmpty();
                resp.put("ok", ok);
                resp.put("vector_dim", ok ? v.size() : 0);
            } else {
                var v = embeddingService.embedText("health check");
                boolean ok = v != null && !v.isEmpty();
                resp.put("ok", ok);
                resp.put("vector_dim", ok ? v.size() : 0);
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("ok", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }

    @GetMapping("/gen")
    public ResponseEntity<?> generationHealth() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("provider", providerName);
        try {
            ContentFragment f = new ContentFragment();
            f.setText("健康检查：请生成一道简短单选题。");
            var g = generationService.generateFromFragment(f);
            boolean ok = g != null && g.stem != null && !g.stem.isBlank();
            resp.put("ok", ok);
            resp.put("type", g.type);
            resp.put("difficulty", g.difficulty);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("ok", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }
}

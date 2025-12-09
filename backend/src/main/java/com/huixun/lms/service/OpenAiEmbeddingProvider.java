package com.huixun.lms.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    private final OpenAiEmbeddingModel model;

    public OpenAiEmbeddingProvider(@Value("${app.llm.openai.api-key}") String apiKey,
                                   @Value("${app.llm.openai.model}") String modelName) {
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            this.model = OpenAiEmbeddingModel.builder().apiKey(apiKey).modelName(modelName).build();
        } else {
            this.model = null;
        }
    }

    @Override
    public List<Double> embed(String text) {
        if (model == null || text == null || text.trim().isEmpty()) return java.util.Collections.emptyList();
        Embedding e = model.embed(text).content();
        float[] v = e.vector();
        java.util.List<Double> out = new java.util.ArrayList<>(v.length);
        for (float f : v) out.add((double) f);
        return out;
    }
}

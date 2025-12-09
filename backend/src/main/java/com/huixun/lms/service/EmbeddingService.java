package com.huixun.lms.service;

import com.huixun.lms.model.ContentFragment;
import com.huixun.lms.model.Document;
import com.huixun.lms.repository.ContentFragmentRepository;
import com.huixun.lms.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    private final ContentFragmentRepository fragmentRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingProvider openAiProvider;
    private final String providerName;
    private final JdbcTemplate jdbcTemplate;

    public EmbeddingService(ContentFragmentRepository fragmentRepository,
                            DocumentRepository documentRepository,
                            OpenAiEmbeddingProvider openAiProvider,
                            @Value("${app.llm.provider}") String providerName,
                            JdbcTemplate jdbcTemplate) {
        this.fragmentRepository = fragmentRepository;
        this.documentRepository = documentRepository;
        this.openAiProvider = openAiProvider;
        this.providerName = providerName;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void embedDocument(Long documentId) throws IOException {
        Document doc = documentRepository.findById(documentId).orElseThrow();
        try {
            List<ContentFragment> fragments = fragmentRepository.findByDocumentId(documentId);
            for (ContentFragment f : fragments) {
                List<Double> vec = null;
                if ("openai".equalsIgnoreCase(providerName)) {
                    vec = openAiProvider.embed(f.getText());
                }
                if (vec == null || vec.isEmpty()) {
                    double[] rv = randomVec(64);
                    f.setEmbeddingJson(toJson(rv));
                } else {
                    f.setEmbeddingJson(toJson(vec));
                }
                try {
                    String vstr = toVectorString(vec != null && !vec.isEmpty() ? vec : java.util.Arrays.stream(randomVec(64)).boxed().toList());
                    jdbcTemplate.update("UPDATE content_fragments SET embedding = '" + vstr + "'::vector WHERE id = ?", f.getId());
                } catch (Exception ignored) {}
            }
            fragmentRepository.saveAll(fragments);
            doc.setStatus("DONE");
            doc.setErrorMessage(null);
            documentRepository.save(doc);
        } catch (Exception e) {
            doc.setStatus("FAILED");
            doc.setErrorMessage(e.getMessage());
            documentRepository.save(doc);
            throw e instanceof IOException ? (IOException) e : new IOException(e);
        }
    }

    public List<Double> embedText(String text) {
        if ("openai".equalsIgnoreCase(providerName)) {
            List<Double> v = openAiProvider.embed(text);
            if (v != null && !v.isEmpty()) return v;
        }
        double[] rv = randomVec(64);
        java.util.List<Double> list = new java.util.ArrayList<>();
        for (double d : rv) list.add(d);
        return list;
    }

    private double[] randomVec(int n) {
        Random rnd = new Random(42);
        double[] v = new double[n];
        for (int i = 0; i < n; i++) v[i] = rnd.nextDouble();
        return v;
    }

    private String toJson(double[] v) {
        return "[" + java.util.Arrays.stream(v).mapToObj(d -> String.format("%.6f", d)).collect(Collectors.joining(",")) + "]";
    }

    private String toJson(List<Double> v) {
        return "[" + v.stream().map(d -> String.format("%.6f", d)).collect(Collectors.joining(",")) + "]";
    }

    private String toVectorString(List<Double> v) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < v.size(); i++) { if (i>0) b.append(','); b.append(String.format("%.6f", v.get(i))); }
        b.append(']');
        return b.toString();
    }
}

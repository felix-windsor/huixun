package com.huixun.lms.service;

import com.huixun.lms.model.ContentFragment;
import com.huixun.lms.repository.ContentFragmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SearchService {
    private final ContentFragmentRepository fragmentRepository;
    private final EmbeddingService embeddingService;
    private final JdbcTemplate jdbcTemplate;

    public SearchService(ContentFragmentRepository fragmentRepository, EmbeddingService embeddingService, JdbcTemplate jdbcTemplate) {
        this.fragmentRepository = fragmentRepository;
        this.embeddingService = embeddingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Result> topK(String query, int k) {
        List<Double> q = embeddingService.embedText(query);
        String vstr = toVectorString(q);
        try {
            return jdbcTemplate.query(
                    "SELECT id, text, section_path, page_range, 1 - (embedding <-> '" + vstr + "'::vector) AS score FROM content_fragments WHERE embedding IS NOT NULL ORDER BY embedding <-> '" + vstr + "'::vector LIMIT ?",
                    ps -> ps.setInt(1, k),
                    (rs, rowNum) -> new Result(map(rs.getLong("id"), rs.getString("text"), rs.getString("section_path"), rs.getString("page_range")), rs.getDouble("score"))
            );
        } catch (Exception e) {
            List<ContentFragment> all = fragmentRepository.findAll();
            List<Result> rs = new ArrayList<>();
            for (ContentFragment f : all) {
                double[] v = parse(f.getEmbeddingJson());
                if (v == null || q == null || q.isEmpty()) continue;
                double s = cosine(q, v);
                rs.add(new Result(f, s));
            }
            rs.sort(Comparator.comparingDouble((Result r) -> r.score).reversed());
            return rs.subList(0, Math.min(k, rs.size()));
        }
    }

    private ContentFragment map(Long id, String text, String sectionPath, String pageRange) {
        ContentFragment f = new ContentFragment();
        try { var idField = ContentFragment.class.getDeclaredField("id"); idField.setAccessible(true); idField.set(f, id); } catch (Exception ignored) {}
        f.setText(text);
        f.setSectionPath(sectionPath);
        f.setPageRange(pageRange);
        return f;
    }

    private String toVectorString(List<Double> v) {
        StringBuilder b = new StringBuilder("[");
        for (int i = 0; i < v.size(); i++) { if (i>0) b.append(','); b.append(String.format("%.6f", v.get(i))); }
        b.append(']');
        return b.toString();
    }

    private double[] parse(String json) {
        if (json == null || json.isBlank()) return null;
        String[] parts = json.replace("[", "").replace("]", "").split(",");
        double[] v = new double[parts.length];
        for (int i = 0; i < parts.length; i++) v[i] = Double.parseDouble(parts[i]);
        return v;
    }

    private double cosine(List<Double> a, double[] b) {
        int n = Math.min(a.size(), b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < n; i++) { dot += a.get(i) * b[i]; na += a.get(i) * a.get(i); nb += b[i] * b[i]; }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    public static class Result {
        public ContentFragment fragment;
        public double score;
        public Result(ContentFragment f, double s){ this.fragment = f; this.score = s; }
    }
}

package com.huixun.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huixun.lms.model.ContentFragment;
import com.huixun.lms.model.Question;
import com.huixun.lms.model.Quiz;
import com.huixun.lms.repository.ContentFragmentRepository;
import com.huixun.lms.repository.CourseRepository;
import com.huixun.lms.repository.QuestionRepository;
import com.huixun.lms.repository.QuizRepository;
import com.huixun.lms.service.QuestionGenerationService;
import com.huixun.lms.service.EmbeddingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ContentFragmentRepository fragmentRepository;
    private final QuestionGenerationService generationService;
    private final EmbeddingService embeddingService;
    private final CourseRepository courseRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuizController(QuizRepository quizRepository,
                          QuestionRepository questionRepository,
                          ContentFragmentRepository fragmentRepository,
                          QuestionGenerationService generationService,
                          EmbeddingService embeddingService,
                          CourseRepository courseRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.fragmentRepository = fragmentRepository;
        this.generationService = generationService;
        this.embeddingService = embeddingService;
        this.courseRepository = courseRepository;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Quiz quiz) {
        if (quiz.getCourse() != null && quiz.getCourse().getId() != null) {
            Long cid = quiz.getCourse().getId();
            var ref = courseRepository.getReferenceById(cid);
            quiz.setCourse(ref);
        }
        quizRepository.save(quiz);
        return ResponseEntity.ok(new IdResponse(quiz.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> get(@PathVariable("id") Long id) {
        return quizRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<?> generate(@PathVariable("id") Long id,
                                      @RequestParam("documentId") Long documentId,
                                      @RequestParam(value = "count", defaultValue = "5") int count,
                                      @RequestParam(value = "type", required = false) String type) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        List<ContentFragment> fragments = fragmentRepository.findByDocumentId(documentId);
        List<Question> qs = new ArrayList<>();
        int n = Math.min(count, fragments.size());
        for (int i = 0; i < n; i++) {
            var g = generationService.generateFromFragment(fragments.get(i), type);
            Question q = new Question();
            q.setQuiz(quiz);
            q.setType(g.type);
            q.setStem(g.stem);
            try {
                q.setOptionsJson(mapper.writeValueAsString(g.options));
            } catch (Exception ignored) { q.setOptionsJson("[]"); }
            q.setAnswer(g.answer);
            q.setDifficulty(g.difficulty);
            q.setKnowledgeTags(String.join(",", g.tags));
            q.setFragmentId(fragments.get(i).getId());
            qs.add(q);
        }
        questionRepository.saveAll(qs);
        List<QuestionDTO> out = qs.stream().map(q -> new QuestionDTO(
                q.getId(), q.getType(), q.getDifficulty(), q.getStem(), q.getOptionsJson(), q.getAnswer(), q.getFragmentId(), qualityScore(q)
        )).toList();
        return ResponseEntity.ok(out);
    }

    @PostMapping("/{id}/generate-by-query")
    public ResponseEntity<?> generateByQuery(@PathVariable("id") Long id,
                                             @RequestParam("documentId") Long documentId,
                                             @RequestParam(value = "count", defaultValue = "5") int count,
                                             @RequestParam(value = "query", required = false) String query,
                                             @RequestParam(value = "tags", required = false) String tags,
                                             @RequestParam(value = "difficulty", required = false) String difficulty,
                                             @RequestParam(value = "type", required = false) String type) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        List<com.huixun.lms.model.ContentFragment> fragments = fragmentRepository.findByDocumentId(documentId);
        List<com.huixun.lms.model.ContentFragment> selected;
        if (query != null && !query.isBlank()) {
            List<Double> qvec = embeddingService.embedText(query);
            selected = fragments.stream()
                    .sorted(Comparator.comparingDouble((com.huixun.lms.model.ContentFragment f) -> -cos(qvec, parseVec(f.getEmbeddingJson()))))
                    .collect(Collectors.toList());
        } else {
            selected = fragments;
        }
        if (tags != null && !tags.isBlank()) {
            String t = tags.trim().toLowerCase();
            selected = selected.stream()
                    .filter(f -> {
                        String kw = f.getKeywords();
                        return kw != null && kw.toLowerCase().contains(t);
                    })
                    .collect(Collectors.toList());
        }
        List<Question> qs = new ArrayList<>();
        int n = Math.min(count, selected.size());
        for (int i = 0; i < n; i++) {
            var g = generationService.generateFromFragment(selected.get(i), type);
            if (difficulty != null && !difficulty.isBlank()) g.difficulty = difficulty;
            if (tags != null && !tags.isBlank()) g.tags = List.of(tags);
            Question q = new Question();
            q.setQuiz(quiz);
            q.setType(g.type);
            q.setStem(g.stem);
            try {
                q.setOptionsJson(mapper.writeValueAsString(g.options));
            } catch (Exception ignored) { q.setOptionsJson("[]"); }
            q.setAnswer(g.answer);
            q.setDifficulty(g.difficulty);
            q.setKnowledgeTags(String.join(",", g.tags));
            q.setFragmentId(selected.get(i).getId());
            qs.add(q);
        }
        questionRepository.saveAll(qs);
        List<QuestionDTO> out = qs.stream().map(q -> new QuestionDTO(
                q.getId(), q.getType(), q.getDifficulty(), q.getStem(), q.getOptionsJson(), q.getAnswer(), q.getFragmentId(), qualityScore(q)
        )).toList();
        return ResponseEntity.ok(out);
    }

    private static double cos(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0.0;
        int n = Math.min(a.size(), b.size());
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < n; i++) { double x = a.get(i); double y = b.get(i); dot += x*y; na += x*x; nb += y*y; }
        if (na == 0 || nb == 0) return 0.0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static List<Double> parseVec(String json) {
        try {
            if (json == null || json.isBlank()) return java.util.Collections.emptyList();
            String s = json.trim();
            if (s.startsWith("[")) s = s.substring(1);
            if (s.endsWith("]")) s = s.substring(0, s.length()-1);
            if (s.isBlank()) return java.util.Collections.emptyList();
            String[] parts = s.split(",");
            List<Double> v = new java.util.ArrayList<>();
            for (String p : parts) {
                try { v.add(Double.parseDouble(p.trim())); } catch (Exception ignored) {}
            }
            return v;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<Quiz> publish(@PathVariable("id") Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        quiz.setStatus("PUBLISHED");
        return ResponseEntity.ok(quizRepository.save(quiz));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<?> questions(@PathVariable("id") Long id) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(questionRepository.findAll().stream().filter(q -> q.getQuiz().getId().equals(quiz.getId())).toList());
    }

    static class IdResponse { public Long id; IdResponse(Long id){this.id=id;} }
    static class QuestionDTO {
        public Long id; public String type; public String difficulty; public String stem; public String optionsJson; public String answer; public Long fragmentId; public int qualityScore;
        public QuestionDTO(Long id, String type, String difficulty, String stem, String optionsJson, String answer, Long fragmentId, int qualityScore){
            this.id=id; this.type=type; this.difficulty=difficulty; this.stem=stem; this.optionsJson=optionsJson; this.answer=answer; this.fragmentId=fragmentId; this.qualityScore=qualityScore;
        }
    }

    private int qualityScore(Question q) {
        try {
            String type = q.getType() == null ? "" : q.getType().trim().toUpperCase();
            String stem = q.getStem() == null ? "" : q.getStem();
            String ans = q.getAnswer() == null ? "" : q.getAnswer();
            java.util.List<String> opts;
            try {
                opts = mapper.readValue(q.getOptionsJson() == null ? "[]" : q.getOptionsJson(), new com.fasterxml.jackson.core.type.TypeReference<java.util.List<String>>(){});
            } catch (Exception e) { opts = java.util.Collections.emptyList(); }
            int score = 0;
            if (stem.length() >= 12 && stem.length() <= 180) score += 20; else if (stem.length() >= 8) score += 10;
            java.util.Set<String> uniq = new java.util.LinkedHashSet<>();
            for (Object o : opts) { if (o != null) uniq.add(String.valueOf(o)); }
            if (uniq.size() >= 4) score += 30; else if (uniq.size() >= 3) score += 15;
            if ("TRUE_FALSE".equals(type)) {
                if (uniq.contains("正确") && uniq.contains("错误")) score += 20;
                if ("正确".equals(ans) || "错误".equals(ans)) score += 20;
            } else if ("MULTI".equals(type)) {
                String[] parts = ans.split("[;,|]");
                int valid = 0;
                for (String p : parts) {
                    String m = p.trim().toUpperCase();
                    if (m.length()==1 && m.charAt(0)>='A' && m.charAt(0)<='Z') {
                        int idx = m.charAt(0) - 'A';
                        if (idx >=0 && idx < uniq.size()) valid++;
                    }
                }
                score += Math.min(30, valid * 15);
                if (valid >= 2) score += 10;
            } else {
                if (uniq.contains(ans)) score += 30; else if (!ans.isBlank()) score += 10;
            }
            if (score > 100) score = 100; if (score < 0) score = 0;
            return score;
        } catch (Exception e) {
            return 50;
        }
    }
}

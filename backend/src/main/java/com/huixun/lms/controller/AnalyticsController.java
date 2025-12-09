package com.huixun.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huixun.lms.model.Attempt;
import com.huixun.lms.model.Question;
import com.huixun.lms.model.Quiz;
import com.huixun.lms.repository.AttemptRepository;
import com.huixun.lms.repository.QuestionRepository;
import com.huixun.lms.repository.QuizRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public AnalyticsController(AttemptRepository attemptRepository, QuizRepository quizRepository, QuestionRepository questionRepository) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
    }

    @GetMapping("/knowledge-points")
    public ResponseEntity<?> knowledgePoints(@RequestParam(required = false) Long quizId) throws Exception {
        List<Attempt> attempts = attemptRepository.findAll();
        if (quizId != null) attempts = attempts.stream().filter(a -> a.getQuiz().getId().equals(quizId)).collect(Collectors.toList());
        Map<Long, List<Question>> questionsByQuiz = new HashMap<>();
        Set<String> tags = new HashSet<>();
        Map<String, int[]> metrics = new HashMap<>();
        for (Attempt a : attempts) {
            Long qid = a.getQuiz().getId();
            List<Question> qs = questionsByQuiz.computeIfAbsent(qid, k -> questionRepository.findAll().stream().filter(q -> q.getQuiz().getId().equals(qid)).toList());
            Map<String, String> ans = a.getAnswersJson() != null ? mapper.readValue(a.getAnswersJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}) : Collections.<String, String>emptyMap();
            for (Question q : qs) {
                if (q.getKnowledgeTags() == null || q.getKnowledgeTags().isBlank()) continue;
                boolean objective = q.getType().equals("SINGLE") || q.getType().equals("TRUE_FALSE") || q.getType().equals("MULTI");
                if (!objective) continue;
                boolean correct = false;
                String av = ans.getOrDefault(String.valueOf(q.getId()), "");
                if (q.getType().equals("SINGLE") || q.getType().equals("TRUE_FALSE")) {
                    correct = av.equalsIgnoreCase(q.getAnswer());
                } else if (q.getType().equals("MULTI")) {
                    Set<String> sa = new HashSet<>(Arrays.asList(av.split(",")));
                    Set<String> sb = new HashSet<>(Arrays.asList(q.getAnswer().split(",")));
                    correct = sa.equals(sb);
                }
                for (String t : q.getKnowledgeTags().split(",")) {
                    String key = t.trim();
                    if (key.isEmpty()) continue;
                    tags.add(key);
                    int[] m = metrics.computeIfAbsent(key, k -> new int[2]);
                    m[1] += 1;
                    if (correct) m[0] += 1;
                }
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (String t : tags) {
            int[] m = metrics.getOrDefault(t, new int[2]);
            double acc = m[1] == 0 ? 0.0 : (double) m[0] / m[1];
            out.add(Map.of("tag", t, "correct", m[0], "total", m[1], "accuracy", acc));
        }
        out.sort(Comparator.comparingDouble(o -> -((Double) o.get("accuracy"))));
        return ResponseEntity.ok(out);
    }

    @GetMapping("/difficulty")
    public ResponseEntity<?> difficulty(@RequestParam Long quizId) throws Exception {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        List<Question> qs = questionRepository.findAll().stream().filter(q -> q.getQuiz().getId().equals(quiz.getId())).toList();
        List<Attempt> attempts = attemptRepository.findAll().stream().filter(a -> a.getQuiz().getId().equals(quizId)).toList();
        Map<String, int[]> metrics = new HashMap<>();
        for (Attempt a : attempts) {
            Map<String, String> ans = a.getAnswersJson() != null ? mapper.readValue(a.getAnswersJson(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {}) : Collections.emptyMap();
            for (Question q : qs) {
                boolean objective = q.getType().equals("SINGLE") || q.getType().equals("TRUE_FALSE") || q.getType().equals("MULTI");
                if (!objective) continue;
                String diff = q.getDifficulty();
                int[] m = metrics.computeIfAbsent(diff, k -> new int[2]);
                m[1] += 1;
                boolean correct = false;
                String av = ans.getOrDefault(String.valueOf(q.getId()), "");
                if (q.getType().equals("SINGLE") || q.getType().equals("TRUE_FALSE")) correct = av.equalsIgnoreCase(q.getAnswer());
                else if (q.getType().equals("MULTI")) {
                    Set<String> sa = new HashSet<>(Arrays.asList(av.split(",")));
                    Set<String> sb = new HashSet<>(Arrays.asList(q.getAnswer().split(",")));
                    correct = sa.equals(sb);
                }
                if (correct) m[0] += 1;
            }
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (String d : List.of("EASY", "MEDIUM", "HARD")) {
            int[] m = metrics.getOrDefault(d, new int[2]);
            double acc = m[1] == 0 ? 0.0 : (double) m[0] / m[1];
            out.add(Map.of("difficulty", d, "correct", m[0], "total", m[1], "accuracy", acc));
        }
        return ResponseEntity.ok(out);
    }
}

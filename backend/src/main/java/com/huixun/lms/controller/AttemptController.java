package com.huixun.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huixun.lms.model.Attempt;
import com.huixun.lms.model.Question;
import com.huixun.lms.model.Quiz;
import com.huixun.lms.model.User;
import com.huixun.lms.repository.AttemptRepository;
import com.huixun.lms.repository.QuestionRepository;
import com.huixun.lms.repository.QuizRepository;
import com.huixun.lms.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public AttemptController(AttemptRepository attemptRepository, QuizRepository quizRepository, QuestionRepository questionRepository, UserRepository userRepository) {
        this.attemptRepository = attemptRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> start(@RequestParam Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        List<Question> qs = questionRepository.findAll().stream().filter(q -> q.getQuiz().getId().equals(quiz.getId())).toList();
        return ResponseEntity.ok(qs);
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<?> submit(@PathVariable("id") Long id, @RequestBody Map<String, String> answers, Authentication auth) throws Exception {
        Attempt attempt = attemptRepository.findById(id).orElseThrow();
        List<Question> qs = questionRepository.findAll().stream().filter(q -> q.getQuiz().getId().equals(attempt.getQuiz().getId())).toList();
        double score = grade(qs, answers);
        attempt.setAnswersJson(mapper.writeValueAsString(answers));
        attempt.setScore(score);
        attemptRepository.save(attempt);
        return ResponseEntity.ok(Map.of("score", score));
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam Long quizId, Authentication auth) {
        String username = auth != null ? auth.getName() : null;
        User user = username != null ? userRepository.findByUsername(username).orElse(null) : null;
        if (user == null) return ResponseEntity.status(401).build();
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        Attempt a = new Attempt();
        a.setQuiz(quiz);
        a.setUser(user);
        a = attemptRepository.save(a);
        return ResponseEntity.ok(Map.of("attemptId", a.getId()));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<?> report(@PathVariable("id") Long id) {
        Attempt a = attemptRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(Map.of("score", a.getScore(), "answers", a.getAnswersJson()));
    }

    private double grade(List<Question> qs, Map<String, String> ans) {
        if (qs.isEmpty()) return 0;
        int correct = 0;
        for (Question q : qs) {
            String a = ans.getOrDefault(String.valueOf(q.getId()), "");
            if (q.getType().equals("SINGLE") || q.getType().equals("TRUE_FALSE")) {
                if (a.equalsIgnoreCase(q.getAnswer())) correct++;
            } else if (q.getType().equals("MULTI")) {
                Set<String> sa = new HashSet<>(Arrays.asList(a.split(",")));
                Set<String> sb = new HashSet<>(Arrays.asList(q.getAnswer().split(",")));
                if (sa.equals(sb)) correct++;
            } else {
                if (!a.isBlank()) correct++;
            }
        }
        return (double) correct / qs.size();
    }
}

package com.huixun.lms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huixun.lms.model.ContentFragment;
import com.huixun.lms.model.Question;
import com.huixun.lms.model.Quiz;
import com.huixun.lms.repository.ContentFragmentRepository;
import com.huixun.lms.repository.QuestionRepository;
import com.huixun.lms.repository.QuizRepository;
import com.huixun.lms.service.QuestionGenerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ContentFragmentRepository fragmentRepository;
    private final QuestionGenerationService generationService;
    private final ObjectMapper mapper = new ObjectMapper();

    public QuizController(QuizRepository quizRepository,
                          QuestionRepository questionRepository,
                          ContentFragmentRepository fragmentRepository,
                          QuestionGenerationService generationService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.fragmentRepository = fragmentRepository;
        this.generationService = generationService;
    }

    @PostMapping
    public ResponseEntity<Quiz> create(@RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizRepository.save(quiz));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> get(@PathVariable("id") Long id) {
        return quizRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/generate")
    public ResponseEntity<?> generate(@PathVariable("id") Long id,
                                      @RequestParam Long documentId,
                                      @RequestParam(defaultValue = "5") int count) {
        Quiz quiz = quizRepository.findById(id).orElseThrow();
        List<ContentFragment> fragments = fragmentRepository.findByDocumentId(documentId);
        List<Question> qs = new ArrayList<>();
        int n = Math.min(count, fragments.size());
        for (int i = 0; i < n; i++) {
            var g = generationService.generateFromFragment(fragments.get(i));
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
        return ResponseEntity.ok(qs);
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
}

package com.huixun.lms.controller;

import com.huixun.lms.model.Question;
import com.huixun.lms.repository.QuestionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping
    public List<Question> list() { return questionRepository.findAll(); }

    @PutMapping("/{id}")
    public Question update(@PathVariable("id") Long id, @RequestBody Question q) {
        Question x = questionRepository.findById(id).orElseThrow();
        x.setType(q.getType());
        x.setStem(q.getStem());
        x.setOptionsJson(q.getOptionsJson());
        x.setAnswer(q.getAnswer());
        x.setDifficulty(q.getDifficulty());
        x.setKnowledgeTags(q.getKnowledgeTags());
        return questionRepository.save(x);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        questionRepository.deleteById(id);
    }
}

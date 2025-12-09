package com.huixun.lms.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Quiz quiz;

    @Column(nullable = false)
    private String type; // SINGLE/MULTI/TRUE_FALSE/SHORT_ANSWER

    @Column(nullable = false, length = 2000)
    private String stem;

    @Column(length = 2000)
    private String optionsJson; // JSON for options

    @Column(length = 1000)
    private String answer;

    @Column(nullable = false)
    private String difficulty; // EASY/MEDIUM/HARD

    private String knowledgeTags; // comma-separated names

    private Long fragmentId; // source ContentFragment id

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStem() { return stem; }
    public void setStem(String stem) { this.stem = stem; }
    public String getOptionsJson() { return optionsJson; }
    public void setOptionsJson(String optionsJson) { this.optionsJson = optionsJson; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getKnowledgeTags() { return knowledgeTags; }
    public void setKnowledgeTags(String knowledgeTags) { this.knowledgeTags = knowledgeTags; }
    public Long getFragmentId() { return fragmentId; }
    public void setFragmentId(Long fragmentId) { this.fragmentId = fragmentId; }
}

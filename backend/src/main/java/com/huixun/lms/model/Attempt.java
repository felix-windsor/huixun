package com.huixun.lms.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "attempts")
public class Attempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Quiz quiz;

    @ManyToOne(optional = false)
    private User user;

    @Column(length = 4000)
    private String answersJson;

    private Double score;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAnswersJson() { return answersJson; }
    public void setAnswersJson(String answersJson) { this.answersJson = answersJson; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
}

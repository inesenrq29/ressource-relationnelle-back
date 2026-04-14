package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QuizQuestion")
@Getter
@Setter
public class QuizQuestion {

  @Id
  @GeneratedValue
  @Column(name = "quizQuestionId", nullable = false, updatable = false)
  private UUID quizQuestionId;

  @Column(name = "question", nullable = false, columnDefinition = "TEXT")
  private String question;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quizId", nullable = false)
  private Quiz quiz;

  @Column(name = "correctAnswer", nullable = false)
  private boolean correctAnswer;
}

package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Quiz")
@Getter
@Setter
public class Quiz {

  @Id
  @GeneratedValue
  @Column(name = "quizId", nullable = false, updatable = false)
  private UUID quizId;

  @Column(name = "createdAt", nullable = false, updatable = false)
  private Instant createdAt;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interactiveResourceId", nullable = false, unique = true)
  private InteractiveResource interactiveResource;

  @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<QuizQuestion> questions = new ArrayList<>();
}

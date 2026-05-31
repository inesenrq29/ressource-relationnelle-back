package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "QuizParticipantAnswer")
@Getter
@Setter
public class QuizParticipantAnswer {

  @Id
  @GeneratedValue
  @Column(name = "quizParticipantAnswerId", nullable = false, updatable = false)
  private UUID quizParticipantAnswerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activityParticipantId", nullable = false)
  private ActivityParticipant activityParticipant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "quizQuestionId", nullable = false)
  private QuizQuestion quizQuestion;

  @Column(name = "answeredAt")
  private Instant answeredAt;

  @Column(name = "userAnswer", nullable = false)
  private boolean userAnswer;
}

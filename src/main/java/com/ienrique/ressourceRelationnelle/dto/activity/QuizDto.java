package com.ienrique.ressourceRelationnelle.dto.activity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizDto {

  private UUID quizId;
  private UUID resourceId;
  private List<QuizQuestionDto> questions;
  private Instant createdAt;
}

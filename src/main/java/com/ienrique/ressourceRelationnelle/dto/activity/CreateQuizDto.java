package com.ienrique.ressourceRelationnelle.dto.activity;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateQuizDto {

  private UUID resourceId;
  private List<QuizQuestionDto> questions;
}

package com.ienrique.ressourceRelationnelle.dto.activity;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CheckQuizAnswerDto {

  @NotNull private UUID quizQuestionId;

  @NotNull private Boolean userAnswer;
}

package com.ienrique.ressourceRelationnelle.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerPollDto {

  @NotNull private UUID activitySessionId;

  @NotNull private UUID pollOptionId;
}

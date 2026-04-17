package com.ienrique.ressourceRelationnelle.dto.activity;

import java.util.UUID;

import com.ienrique.ressourceRelationnelle.entity.ActivityType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityResponseDto {

  private UUID activitySessionId;
  @NotNull private ActivityType activityType;
  private StartQuizDto quiz;
  private PollDto poll;
}

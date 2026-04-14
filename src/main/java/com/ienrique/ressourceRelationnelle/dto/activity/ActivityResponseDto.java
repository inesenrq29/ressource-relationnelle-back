package com.ienrique.ressourceRelationnelle.dto.activity;

import com.ienrique.ressourceRelationnelle.entity.ActivityType;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivityResponseDto {
  @NotNull private ActivityType activityType;
  private StartQuizDto quiz;
  private PollDto poll;
}

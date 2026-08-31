package com.ienrique.ressourceRelationnelle.dto.activity;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePollDto {

  @NotNull private UUID resourceId;

  @NotBlank private String question;

  @NotNull private List<String> options;
}

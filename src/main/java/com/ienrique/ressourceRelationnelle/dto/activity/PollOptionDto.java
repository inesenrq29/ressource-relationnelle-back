package com.ienrique.ressourceRelationnelle.dto.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PollOptionDto {

  @NotBlank
  @Size(max = 100)
  private String optionLabel;
}

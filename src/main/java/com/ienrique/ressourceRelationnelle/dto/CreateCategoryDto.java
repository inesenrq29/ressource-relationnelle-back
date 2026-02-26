package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCategoryDto {

  @NotBlank
  @Size(max = 100)
  private String name;
}

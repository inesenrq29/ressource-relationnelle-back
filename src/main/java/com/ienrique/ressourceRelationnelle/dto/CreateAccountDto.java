package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountDto {

  @NotBlank
  @Size(min = 3, max = 100)
  private String pseudo;

  @NotNull private RoleDto role;
}

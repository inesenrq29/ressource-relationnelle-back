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

  @NotBlank
  @Size(max = 255)
  private String mail;

  @NotBlank
  @Size(min = 8, max = 255)
  private String password;

  @NotNull private RoleDto role;
}

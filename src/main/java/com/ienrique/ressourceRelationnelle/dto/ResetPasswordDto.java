package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDto {

  @NotBlank
  @Size(max = 512)
  private String token;

  @NotBlank
  @Size(min = 8, max = 255)
  private String newPassword;
}

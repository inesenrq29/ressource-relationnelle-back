package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.Email;
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
public class LoginDto {

  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @NotBlank
  @Size(min = 8, max = 255)
  private String password;
}

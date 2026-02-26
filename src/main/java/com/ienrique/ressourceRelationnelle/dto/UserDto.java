package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

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
public class UserDto {

  @NotNull private UUID userId;

  @NotBlank
  @Size(max = 254)
  private String mail;

  @NotBlank
  @Size(max = 50)
  private String role;

  @NotBlank
  @Size(max = 100)
  private String pseudo;

  @NotNull private Instant createdAt;

  @NotNull private Instant updatedAt;

  private boolean isActive;
}

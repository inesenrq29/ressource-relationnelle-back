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

  @NotNull private UUID appUserId;

  @NotBlank
  @Size(max = 255)
  private String mail;

  @NotBlank
  @Size(max = 100)
  private String pseudo;

  private boolean appUserIsActive;

  private Instant lastConnectionAt;
}

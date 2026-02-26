package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FavoriteDto {

  @NotNull private UUID favoriteId;

  @NotNull private UUID resourceId;

  @NotNull private UUID userId;

  @NotNull private Instant createdAt;
}

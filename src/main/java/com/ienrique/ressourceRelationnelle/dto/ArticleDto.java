package com.ienrique.ressourceRelationnelle.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ArticleDto {

  @NotNull private UUID articleId;

  @Size(max = 255)
  private String link;
}

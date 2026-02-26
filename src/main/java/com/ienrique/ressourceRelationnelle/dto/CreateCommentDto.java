package com.ienrique.ressourceRelationnelle.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCommentDto {

  @NotBlank
  @Size(max = 5000)
  private String commentsContent;

  private UUID parentId; // facultatif (null si commentaire principal)
}

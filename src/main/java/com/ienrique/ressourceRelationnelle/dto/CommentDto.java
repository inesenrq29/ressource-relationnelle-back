package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CommentDto {

  @NotNull private UUID commentsId;

  @NotBlank
  @Size(max = 255)
  private String titleComments;

  @NotBlank
  @Size(max = 255)
  private String commentsContent;

  @NotNull private UUID authorId;

  private boolean isModerated;

  @NotNull private Instant createdAt;

  private UUID parentId; // null si commentaire principal

  @NotNull private UUID resourceId;
}

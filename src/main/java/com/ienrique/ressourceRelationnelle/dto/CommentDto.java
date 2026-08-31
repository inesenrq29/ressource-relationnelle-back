package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.entity.CommentStatus;

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
public class CommentDto {

  @NotNull private UUID commentsId;

  @NotNull private Instant publicationDate;

  @NotNull private Instant modificationCommentsDate;

  @NotBlank
  @Size(max = 100)
  private String author;

  @Size(max = 100)
  private String titleComments;

  @NotBlank
  @Size(max = 5000)
  private String commentsContent;

  @NotNull private UUID resourceId;

  @NotNull private CommentStatus status;
}

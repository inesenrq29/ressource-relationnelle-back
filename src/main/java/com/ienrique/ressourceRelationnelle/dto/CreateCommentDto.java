package com.ienrique.ressourceRelationnelle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateCommentDto {

  @Size(max = 100)
  private String titleComments;

  @NotBlank
  @Size(max = 5000)
  private String commentsContent;
}

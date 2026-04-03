package com.ienrique.ressourceRelationnelle.dto;

import com.ienrique.ressourceRelationnelle.entity.CommentStatus;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModerateCommentDto {

  private CommentStatus status;

  @Size(max = 500)
  private String moderationReason;
}

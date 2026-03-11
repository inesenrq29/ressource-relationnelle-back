package com.ienrique.ressourceRelationnelle.dto;

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

  private boolean isModerated;

  @Size(max = 500)
  private String reason;
}

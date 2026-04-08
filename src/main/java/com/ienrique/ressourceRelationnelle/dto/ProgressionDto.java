package com.ienrique.ressourceRelationnelle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProgressionDto {

  private long favoritesCount;

  private long exploitedCount;

  private long setAsideCount;
}

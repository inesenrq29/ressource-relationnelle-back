package com.ienrique.ressourceRelationnelle.dto;

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
public class VideoDto {

  @NotNull private UUID videoId;

  @NotBlank
  @Size(max = 50)
  private String videoWeight;
}

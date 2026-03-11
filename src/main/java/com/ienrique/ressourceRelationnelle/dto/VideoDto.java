package com.ienrique.ressourceRelationnelle.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
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

  @NotNull private Long videoWeight;
}

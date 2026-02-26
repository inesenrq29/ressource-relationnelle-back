package com.ienrique.ressourceRelationnelle.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateResourceDto {

  @NotBlank
  @Size(max = 255)
  private String title;

  @NotBlank
  @Size(max = 5000)
  private String description;

  @NotEmpty private List<@NotBlank String> tags;

  private boolean isRestricted;

  private boolean isPublished;

  @NotNull private UUID categoryId;
}

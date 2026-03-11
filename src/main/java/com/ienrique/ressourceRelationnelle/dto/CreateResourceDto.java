package com.ienrique.ressourceRelationnelle.dto;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;

import jakarta.validation.constraints.NotBlank;
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
  private String resourceTitle;

  @Size(max = 5000)
  private String resourceDescription;

  @NotNull private ResourceStatus status;

  private boolean resourceIsUsed;

  @NotNull private UUID categoryId;

  private List<@NotBlank @Size(max = 140) String> tags;
}

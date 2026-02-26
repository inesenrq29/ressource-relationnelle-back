package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.List;
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
public class ResourceDto {

  @NotNull private UUID resourceId;

  @NotBlank
  @Size(max = 255)
  private String title;

  @NotBlank
  @Size(max = 5000)
  private String description;

  private boolean isRestricted;
  private boolean isPublished;
  private boolean isExploited;
  private boolean isSuspended;

  @NotNull private List<@NotBlank @Size(max = 50) String> tags;

  @NotNull private Instant createdAt;

  @NotNull private Instant updatedAt;

  @NotNull private UUID categoryId;

  @NotBlank
  @Size(max = 255)
  private String createdBy;
}

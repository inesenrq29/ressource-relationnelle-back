package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;
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

  private boolean resourceIsActive;

  private boolean resourceIsUsed;

  @NotBlank
  @Size(max = 255)
  private String resourceTitle;

  @Size(max = 5000)
  private String resourceDescription;

  @NotNull private ResourceStatus status;

  @NotNull private Instant resourceCreatedAt;

  @NotNull private UUID categoryId;

  @NotBlank
  @Size(max = 100)
  private String categoryName;

  @NotNull private List<@NotBlank @Size(max = 140) String> tags;
}

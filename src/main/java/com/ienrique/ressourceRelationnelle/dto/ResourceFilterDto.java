package com.ienrique.ressourceRelationnelle.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResourceFilterDto {

  @Size(max = 255)
  private String title;

  private boolean isRestricted;
  private boolean isPublished;
  private boolean isExploited;
  private boolean isSuspended;

  private List<@Size(max = 50) String> tags;

  @Size(max = 50)
  private String sortBy;

  @Size(max = 4)
  private String sortDir;

  @Min(0)
  private Integer page;

  @Min(1)
  private Integer size;

  private UUID categoryId;
}

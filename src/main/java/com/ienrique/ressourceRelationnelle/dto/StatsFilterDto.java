package com.ienrique.ressourceRelationnelle.dto;

import java.time.Instant;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.entity.ResourceType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatsFilterDto {
  private ResourceType resourceType;

  private UUID categoryId;

  private Instant startDate;

  private Instant endDate;
}

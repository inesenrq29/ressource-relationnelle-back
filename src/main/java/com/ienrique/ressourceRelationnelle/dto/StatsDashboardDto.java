package com.ienrique.ressourceRelationnelle.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatsDashboardDto {

  private long exploitedResourcesCount;
  private long totalResourcesCount;
  private Map<String, Long> resourcesByCategory = new HashMap<>();
  private Map<String, Long> resourcesByType = new HashMap<>();
}

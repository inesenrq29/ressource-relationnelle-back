package com.ienrique.ressourceRelationnelle.service;

import com.ienrique.ressourceRelationnelle.dto.StatsDashboardDto;
import com.ienrique.ressourceRelationnelle.dto.StatsFilterDto;

public interface StatService {

  StatsDashboardDto getStats();

  StatsDashboardDto getStats(StatsFilterDto filter);

  byte[] exportStats(StatsFilterDto filter);
}

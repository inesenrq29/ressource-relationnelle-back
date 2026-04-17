package com.ienrique.ressourceRelationnelle.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ienrique.ressourceRelationnelle.dto.StatsDashboardDto;
import com.ienrique.ressourceRelationnelle.dto.StatsFilterDto;
import com.ienrique.ressourceRelationnelle.service.StatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatController {

  private final StatService statService;

  @GetMapping
  public ResponseEntity<StatsDashboardDto> getStats() {
    return ResponseEntity.ok(statService.getStats());
  }

  @PostMapping("/filter")
  public ResponseEntity<StatsDashboardDto> getFilteredStats(
      @RequestBody final StatsFilterDto filter) {
    return ResponseEntity.ok(statService.getStats(filter));
  }

  @PostMapping("/export")
  public ResponseEntity<byte[]> exportStats(@RequestBody final StatsFilterDto filter) {

    final byte[] file = statService.exportStats(filter);

    return ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=stats.csv")
        .header("Content-Type", "text/csv")
        .body(file);
  }
}

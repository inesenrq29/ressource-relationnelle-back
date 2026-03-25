package com.ienrique.ressourceRelationnelle.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ienrique.ressourceRelationnelle.dto.CreateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.ResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceStatusDto;
import com.ienrique.ressourceRelationnelle.service.ResourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

  private final ResourceService resourceService;

  @GetMapping("/{resourceId}")
  public ResponseEntity<ResourceDto> getResourceById(final @PathVariable UUID resourceId) {
    final ResourceDto response = resourceService.getResourceById(resourceId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<ResourceDto>> getResources() {
    final List<ResourceDto> response = resourceService.getResources();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/restricted")
  public ResponseEntity<List<ResourceDto>> getRestrictedResources() {
    final List<ResourceDto> response = resourceService.getRestrictedResources();
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ResourceDto> createResource(
      @Valid @RequestBody final CreateResourceDto createResourceDto) {
    final ResourceDto response = resourceService.createResource(createResourceDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/{resourceId}")
  public ResponseEntity<Void> updateResource(
      @PathVariable final UUID resourceId, final @Valid @RequestBody UpdateResourceDto request) {
    resourceService.updateResource(resourceId, request);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{resourceId}")
  public ResponseEntity<Void> deleteResource(@PathVariable final UUID resourceId) {
    resourceService.deleteResource(resourceId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{resourceId}/submit")
  public ResponseEntity<Void> submitForValidation(@PathVariable final UUID resourceId) {
    resourceService.submitForValidation(resourceId);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/{resourceId}/status")
  public ResponseEntity<Void> updateResourceStatus(
      @PathVariable final UUID resourceId,
      final @Valid @RequestBody UpdateResourceStatusDto request) {
    resourceService.updateResourceStatus(resourceId, request);
    return ResponseEntity.noContent().build();
  }
}

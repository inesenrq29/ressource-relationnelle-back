package com.ienrique.ressourceRelationnelle.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ienrique.ressourceRelationnelle.dto.*;
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

  @GetMapping("/filter")
  public ResponseEntity<List<ResourceDto>> filterResources(
      @RequestParam(required = false) final String filter) {
    return ResponseEntity.ok(resourceService.filterResources(filter));
  }

  @PostMapping("/{userId}/favorite/{resourceId}")
  public ResponseEntity<Void> addResourceToFavorite(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.addResourceToFavorite(userId, resourceId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{userId}/favorite/{resourceId}")
  public ResponseEntity<Void> removeResourceFromFavorite(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.removeResourceFromFavorite(userId, resourceId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/{userId}/set-aside/{resourceId}")
  public ResponseEntity<Void> setAsideResource(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.setAsideResource(userId, resourceId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{userId}/set-aside/{resourceId}")
  public ResponseEntity<Void> unsetAsideResource(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.unsetAsideResource(userId, resourceId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @PostMapping("/{userId}/exploited/{resourceId}")
  public ResponseEntity<Void> markResourceAsExploited(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.markResourceAsExploited(userId, resourceId);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{userId}/exploited/{resourceId}")
  public ResponseEntity<Void> markResourceAsUnexploited(
      @PathVariable final UUID userId, @PathVariable final UUID resourceId) {
    resourceService.markResourceAsUnexploited(userId, resourceId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping("/{userId}/progressions")
  public ResponseEntity<ProgressionDto> getProgression(@PathVariable final UUID userId) {
    return ResponseEntity.ok(resourceService.getProgression(userId));
  }

  @GetMapping("/sorted")
  public ResponseEntity<List<ResourceDto>> sortResources(
      @RequestParam(defaultValue = "false") final boolean isAscending) {
    return ResponseEntity.ok(resourceService.sortResources(isAscending));
  }

  @PostMapping("/{resourceId}/share/{friendId}")
  public ResponseEntity<Void> shareResource(
      @PathVariable final UUID resourceId,
      @PathVariable final UUID friendId,
      @RequestBody @Valid final ShareResourceRequestDto request) {

    resourceService.shareResource(resourceId, friendId, request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/shares/{sharedResourceId}")
  public ResponseEntity<SharedResourceDto> getSharedResource(
      @PathVariable final UUID sharedResourceId) {
    return ResponseEntity.ok(resourceService.getSharedResource(sharedResourceId));
  }
}

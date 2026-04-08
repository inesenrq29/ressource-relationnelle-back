package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.*;

public interface ResourceService {
  ResourceDto getResourceById(UUID resourceId);

  List<ResourceDto> getResources();

  List<ResourceDto> getRestrictedResources();

  ResourceDto createResource(CreateResourceDto createResource);

  void updateResource(UUID resourceId, UpdateResourceDto updateResource);

  void deleteResource(UUID resourceId);

  void submitForValidation(UUID resourceId);

  void updateResourceStatus(UUID resourceId, UpdateResourceStatusDto status);

  // TODO: faire shareResource
  List<ResourceDto> sortResources(boolean isAscending);

  List<ResourceDto> filterResources(String rsqlQuery);

  void addResourceToFavorite(UUID userId, UUID resourceId);

  void removeResourceFromFavorite(UUID userId, UUID resourceId);

  void setAsideResource(UUID userId, UUID resourceId);

  void unsetAsideResource(UUID userId, UUID resourceId);

  void markResourceAsExploited(UUID userId, UUID resourceId);

  void markResourceAsUnexploited(UUID userId, UUID resourceId);

  ProgressionDto getProgression(UUID userId);
}

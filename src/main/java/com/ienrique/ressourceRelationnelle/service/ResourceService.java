package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.CreateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.ResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceStatusDto;

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
}

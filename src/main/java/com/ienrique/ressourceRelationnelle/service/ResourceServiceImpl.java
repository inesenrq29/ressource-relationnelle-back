package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ienrique.ressourceRelationnelle.dto.CreateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.ResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceDto;
import com.ienrique.ressourceRelationnelle.dto.UpdateResourceStatusDto;
import com.ienrique.ressourceRelationnelle.entity.Category;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;
import com.ienrique.ressourceRelationnelle.entity.Tag;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.ResourceMapper;
import com.ienrique.ressourceRelationnelle.repository.CategoryRepository;
import com.ienrique.ressourceRelationnelle.repository.ResourceRepository;
import com.ienrique.ressourceRelationnelle.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {

  private final ResourceRepository resourceRepository;
  private final CategoryRepository categoryRepository;
  private final TagRepository tagRepository;
  private final ResourceMapper resourceMapper;

  @Override
  public ResourceDto getResourceById(UUID resourceId) {
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    return resourceMapper.toDto(resource);
  }

  @Override
  public List<ResourceDto> getResources() {
    return resourceRepository.findAll().stream().map(resourceMapper::toDto).toList();
  }

  @Override
  public List<ResourceDto> getRestrictedResources() {
    final List<Resource> resources = resourceRepository.findAllByStatus(ResourceStatus.RESTRICTED);

    return resources.stream().map(resourceMapper::toDto).toList();
  }

  @Override
  public ResourceDto createResource(CreateResourceDto createResource) {
    final Category category =
        categoryRepository
            .findById(createResource.getCategoryId())
            .orElseThrow(() -> new NotFoundException("Category not found"));

    final Resource resource = new Resource();
    resource.setResourceTitle(createResource.getResourceTitle());
    resource.setResourceDescription(createResource.getResourceDescription());
    resource.setResourceIsUsed(createResource.isResourceIsUsed());
    resource.setResourceIsActive(true);
    resource.setCategory(category);

    // par défaut on met en DRAFT
    resource.setStatus(ResourceStatus.DRAFT);

    if (createResource.getTags() != null && !createResource.getTags().isEmpty()) {
      // récupération de la liste
      final Set<Tag> tags =
          createResource.getTags().stream()
              // suppression des espaces
              .map(String::trim)
              // suppression des tags vides
              .filter(tagName -> !tagName.isBlank())
              // récupération des tags déjà en base
              .map(
                  tagName ->
                      tagRepository
                          .findByWording(tagName)
                          // s'il n'existe pas
                          .orElseGet(
                              () -> {
                                // on le crée et on enregistre en base
                                final Tag tag = new Tag();
                                tag.setWording(tagName);
                                return tagRepository.save(tag);
                              }))
              // on convertit en Set
              .collect(Collectors.toSet());

      resource.setTags(tags);
    }

    final Resource savedResource = resourceRepository.save(resource);

    return resourceMapper.toDto(savedResource);
  }

  @Override
  public void updateResource(UUID resourceId, UpdateResourceDto updateResource) {
    final Category category =
        categoryRepository
            .findById(updateResource.getCategoryId())
            .orElseThrow(() -> new NotFoundException("Category not found"));

    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    resource.setResourceTitle(updateResource.getResourceTitle());
    resource.setResourceDescription(updateResource.getResourceDescription());
    resource.setResourceIsUsed(updateResource.isResourceIsUsed());
    resource.setCategory(category);

    if (updateResource.getTags() != null) {
      final Set<Tag> tags =
          updateResource.getTags().stream()
              .map(String::trim)
              .filter(tagName -> !tagName.isBlank())
              .map(
                  tagName ->
                      tagRepository
                          .findByWording(tagName)
                          .orElseGet(
                              () -> {
                                final Tag tag = new Tag();
                                tag.setWording(tagName);
                                return tagRepository.save(tag);
                              }))
              .collect(Collectors.toSet());

      resource.setTags(tags);
    }

    resourceRepository.save(resource);
  }

  @Override
  public void deleteResource(UUID resourceId) {
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    resourceRepository.delete(resource);
  }

  @Override
  public void submitForValidation(UUID resourceId) {
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    validateStatusTransition(resource.getStatus(), ResourceStatus.PENDING_VALIDATION);

    resource.setStatus(ResourceStatus.PENDING_VALIDATION);
    resourceRepository.save(resource);
  }

  @Override
  public void updateResourceStatus(UUID resourceId, UpdateResourceStatusDto status) {
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    final ResourceStatus currentStatus = resource.getStatus();
    final ResourceStatus newStatus = status.getStatus();

    validateStatusTransition(currentStatus, newStatus);

    resource.setStatus(newStatus);

    resourceRepository.save(resource);
  }

  private void validateStatusTransition(ResourceStatus current, ResourceStatus next) {
    if (current == next) {
      throw new BadRequestException("Resource already has this status");
    }

    if (current == ResourceStatus.DRAFT && next != ResourceStatus.PENDING_VALIDATION) {
      throw new BadRequestException("Draft can only go to pending validation");
    }

    if (current == ResourceStatus.PENDING_VALIDATION
        && next != ResourceStatus.PUBLISHED
        && next != ResourceStatus.RESTRICTED) {
      throw new BadRequestException("Pending validation can only be published or restricted");
    }

    if (current == ResourceStatus.PUBLISHED
        && next != ResourceStatus.ARCHIVED
        && next != ResourceStatus.RESTRICTED) {
      throw new BadRequestException("Published can only be archived or restricted");
    }

    if (current == ResourceStatus.RESTRICTED
        && next != ResourceStatus.PUBLISHED
        && next != ResourceStatus.ARCHIVED) {
      throw new BadRequestException("Restricted can only go to published or archived");
    }

    if (current == ResourceStatus.ARCHIVED) {
      throw new BadRequestException("Archived resource cannot be modified");
    }
  }
}

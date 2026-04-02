package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.ienrique.ressourceRelationnelle.dto.*;
import com.ienrique.ressourceRelationnelle.entity.*;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.FavoriteMapper;
import com.ienrique.ressourceRelationnelle.mapper.ResourceMapper;
import com.ienrique.ressourceRelationnelle.repository.*;
import com.ienrique.ressourceRelationnelle.service.ResourceServiceImpl;

import io.github.perplexhub.rsql.RSQLJPASupport;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

  @Mock private ResourceRepository resourceRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private AppUserRepository userRepository;
  @Mock private FavoriteRepository favoriteRepository;
  @Mock private TagRepository tagRepository;
  @Mock private ResourceMapper resourceMapper;
  @Mock private FavoriteMapper favoriteMapper;

  @InjectMocks private ResourceServiceImpl resourceService;

  @Nested
  @DisplayName("get resource by id")
  class GetResourceById {

    @Test
    @DisplayName("should return resource by id")
    void shouldReturnResourceById() {
      final Resource resource = new Resource();
      final UUID resourceId = UUID.randomUUID();
      final ResourceDto expectedDto = new ResourceDto();

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(resourceMapper.toDto(resource)).thenReturn(expectedDto);

      final ResourceDto response = resourceService.getResourceById(resourceId);

      assertEquals(expectedDto, response);
    }

    @Test
    @DisplayName("should throw not found exception when resource does not exist")
    void shouldThrowNotFoundException() {
      final UUID resourceId = UUID.randomUUID();

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(NotFoundException.class, () -> resourceService.getResourceById(resourceId));

      assertEquals("Resource not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("get resources")
  class GetResources {

    @Test
    @DisplayName("should return all resources")
    void shouldReturnResources() {
      final Resource resource1 = new Resource();
      final Resource resource2 = new Resource();
      final ResourceDto resourceDto1 = new ResourceDto();
      final ResourceDto resourceDto2 = new ResourceDto();

      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2));
      when(resourceMapper.toDto(resource1)).thenReturn(resourceDto1);
      when(resourceMapper.toDto(resource2)).thenReturn(resourceDto2);

      final List<ResourceDto> resources = resourceService.getResources();

      assertEquals(2, resources.size());
      assertEquals(resourceDto1, resources.getFirst());
      assertEquals(resourceDto2, resources.get(1));
    }
  }

  @Nested
  @DisplayName("get restricted resources")
  class GetRestrictedResources {

    @Test
    @DisplayName("should return all restricted resources")
    void shouldReturnAllRestrictedResources() {
      final Resource resource1 = new Resource();
      final Resource resource2 = new Resource();
      final ResourceDto resourceDto1 = new ResourceDto();
      final ResourceDto resourceDto2 = new ResourceDto();

      when(resourceRepository.findAllByStatus(ResourceStatus.RESTRICTED))
          .thenReturn(List.of(resource1, resource2));
      when(resourceMapper.toDto(resource1)).thenReturn(resourceDto1);
      when(resourceMapper.toDto(resource2)).thenReturn(resourceDto2);

      final List<ResourceDto> resources = resourceService.getRestrictedResources();

      assertEquals(2, resources.size());
      assertEquals(resourceDto1, resources.getFirst());
      assertEquals(resourceDto2, resources.get(1));
    }
  }

  @Nested
  @DisplayName("create resource")
  class CreateResource {

    @Test
    @DisplayName("should create resource")
    void shouldCreateResource() {
      final UUID categoryId = UUID.randomUUID();
      final CreateResourceDto createResource = new CreateResourceDto();
      createResource.setCategoryId(categoryId);
      createResource.setResourceTitle("resource title");
      createResource.setResourceDescription("resource description");
      createResource.setResourceIsUsed(true);
      createResource.setTags(List.of("tag1", "tag2"));

      final Category category = new Category();
      category.setCategoryId(categoryId);
      final Tag tag1 = new Tag();
      tag1.setWording("tag1");
      final Tag tag2 = new Tag();
      tag2.setWording("tag2");

      final Resource savedResource = new Resource();
      final ResourceDto expectedDto = new ResourceDto();

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(tagRepository.findByWording("tag1")).thenReturn(Optional.of(tag1));
      when(tagRepository.findByWording("tag2")).thenReturn(Optional.of(tag2));
      when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);
      when(resourceMapper.toDto(savedResource)).thenReturn(expectedDto);

      final ResourceDto result = resourceService.createResource(createResource);

      assertEquals(expectedDto, result);
      verify(categoryRepository).findById(categoryId);
      verify(tagRepository).findByWording("tag1");
      verify(tagRepository).findByWording("tag2");
      verify(resourceRepository).save(any(Resource.class));
      verify(resourceMapper).toDto(savedResource);
    }

    @Test
    @DisplayName("should throw category not found")
    void shouldThrowCategoryNotFound() {
      final UUID categoryId = UUID.randomUUID();
      final CreateResourceDto createResource = new CreateResourceDto();
      createResource.setCategoryId(categoryId);

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class, () -> resourceService.createResource(createResource));

      assertEquals("Category not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("delete resource")
  class DeleteResource {

    @Test
    @DisplayName("should delete resource")
    void shouldDeleteResource() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      resourceService.deleteResource(resourceId);
    }

    @Test
    @DisplayName("should throw resource not found")
    void shouldThrowResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(NotFoundException.class, () -> resourceService.deleteResource(resourceId));

      assertEquals("Resource not found", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("submit for validation")
  class SubmitForValidation {

    @Test
    @DisplayName("should submit resource for validation")
    void shouldSubmitForValidation() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.DRAFT);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final Resource validatedResource = new Resource();
      validatedResource.setStatus(ResourceStatus.PENDING_VALIDATION);

      resourceRepository.save(validatedResource);

      resourceService.submitForValidation(resourceId);
    }

    @Test
    @DisplayName("should throw resource not found")
    void shouldThrowResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.DRAFT);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class, () -> resourceService.submitForValidation(resourceId));

      assertEquals("Resource not found", exception.getMessage());
    }

    @Test
    @DisplayName("should throw bad request")
    void shouldThrowBadRequest() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.ARCHIVED);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class, () -> resourceService.submitForValidation(resourceId));

      assertEquals("Archived resource cannot be modified", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("update resource status")
  class UpdateResourceStatus {

    @Test
    @DisplayName("should update resource status")
    void shouldUpdateResourceStatus() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.RESTRICTED);
      final UpdateResourceStatusDto status = new UpdateResourceStatusDto();
      status.setStatus(ResourceStatus.PUBLISHED);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      resourceService.updateResourceStatus(resourceId, status);

      resourceRepository.save(resource);
    }

    @Test
    @DisplayName("should throw bad request published status")
    void shouldThrowBadRequestPublishedStatus() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.PUBLISHED);
      final UpdateResourceStatusDto status = new UpdateResourceStatusDto();
      status.setStatus(ResourceStatus.DRAFT);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> resourceService.updateResourceStatus(resourceId, status));

      assertEquals("Published can only be archived or restricted", exception.getMessage());
    }

    @Test
    @DisplayName("should throw bad request restricted status")
    void shouldThrowBadRequestRestrictedStatus() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.RESTRICTED);
      final UpdateResourceStatusDto status = new UpdateResourceStatusDto();
      status.setStatus(ResourceStatus.DRAFT);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> resourceService.updateResourceStatus(resourceId, status));

      assertEquals("Restricted can only go to published or archived", exception.getMessage());
    }

    @Test
    @DisplayName("should throw bad request pending validation status")
    void shouldThrowBadRequestPendingValidationStatus() {
      final UUID resourceId = UUID.randomUUID();
      final Resource resource = new Resource();
      resource.setStatus(ResourceStatus.PENDING_VALIDATION);
      final UpdateResourceStatusDto status = new UpdateResourceStatusDto();
      status.setStatus(ResourceStatus.ARCHIVED);

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> resourceService.updateResourceStatus(resourceId, status));

      assertEquals(
          "Pending validation can only be published or restricted", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("update resource")
  class UpdateResource {

    @Test
    @DisplayName("should update resource")
    void shouldUpdateResource() {
      final UUID resourceId = UUID.randomUUID();
      final UUID categoryId = UUID.randomUUID();
      final Category category = new Category();
      category.setCategoryId(categoryId);
      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final Tag tag1 = new Tag();
      tag1.setWording("tag1");
      final Tag tag2 = new Tag();
      tag2.setWording("tag2");

      final UpdateResourceDto updateResourceDto = new UpdateResourceDto();
      updateResourceDto.setCategoryId(categoryId);
      updateResourceDto.setResourceDescription("description updated");
      updateResourceDto.setResourceTitle("title updated");
      updateResourceDto.setResourceIsUsed(true);
      updateResourceDto.setTags(List.of("tag1", "tag2"));

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(tagRepository.findByWording("tag1")).thenReturn(Optional.of(tag1));
      when(tagRepository.findByWording("tag2")).thenReturn(Optional.of(tag2));

      resourceService.updateResource(resourceId, updateResourceDto);

      verify(categoryRepository).findById(categoryId);
      verify(tagRepository).findByWording("tag1");
      verify(tagRepository).findByWording("tag2");
      verify(resourceRepository).save(any(Resource.class));
    }

    @Test
    @DisplayName("Should throw category not found")
    void shouldThrowCategoryNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final UUID categoryId = UUID.randomUUID();

      final UpdateResourceDto updateResourceDto = new UpdateResourceDto();
      updateResourceDto.setCategoryId(categoryId);

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> resourceService.updateResource(resourceId, updateResourceDto));

      assertEquals("Category not found", exception.getMessage());

      verify(resourceRepository, never()).findByResourceId(any());
      verify(resourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when resource not found")
    void shouldThrowNotFoundExceptionWhenResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final UUID categoryId = UUID.randomUUID();

      final UpdateResourceDto dto = new UpdateResourceDto();
      dto.setCategoryId(categoryId);

      final Category category = new Category();

      when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class, () -> resourceService.updateResource(resourceId, dto));

      assertEquals("Resource not found", exception.getMessage());

      verify(resourceRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("filterResources")
  class FilterResourcesTest {

    @Test
    @DisplayName("should filter resources with specification when rsql query is provided")
    void shouldFilterResourcesWithSpecificationWhenRsqlQueryIsProvided() {
      final String rsqlQuery = "resourceType==PDF";

      final Resource resource = new Resource();
      final ResourceDto resourceDto = new ResourceDto();

      final List<Resource> resources = List.of(resource);
      final List<ResourceDto> resourceDtos = List.of(resourceDto);

      final Specification<Resource> specification = (root, query, criteriaBuilder) -> null;

      try (MockedStatic<RSQLJPASupport> mockedStatic = Mockito.mockStatic(RSQLJPASupport.class)) {
        mockedStatic
            .when(() -> RSQLJPASupport.toSpecification(rsqlQuery))
            .thenReturn(specification);

        when(resourceRepository.findAll(specification)).thenReturn(resources);
        when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

        final List<ResourceDto> result = resourceService.filterResources(rsqlQuery);

        assertEquals(resourceDtos, result);
        mockedStatic.verify(() -> RSQLJPASupport.toSpecification(rsqlQuery));
        verify(resourceRepository).findAll(specification);
        verify(resourceMapper).toDtos(resources);
        verifyNoMoreInteractions(resourceRepository, resourceMapper);
      }
    }

    @Test
    @DisplayName("should return all resources when rsql query is null")
    void shouldReturnAllResourcesWhenRsqlQueryIsNull() {
      final Resource resource = new Resource();
      final ResourceDto resourceDto = new ResourceDto();

      final List<Resource> resources = List.of(resource);
      final List<ResourceDto> resourceDtos = List.of(resourceDto);

      when(resourceRepository.findAll()).thenReturn(resources);
      when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

      final List<ResourceDto> result = resourceService.filterResources(null);

      assertEquals(resourceDtos, result);
      verify(resourceRepository).findAll();
      verify(resourceMapper).toDtos(resources);
      verifyNoMoreInteractions(resourceRepository, resourceMapper);
    }

    @Test
    @DisplayName("should return all resources when rsql query is blank")
    void shouldReturnAllResourcesWhenRsqlQueryIsBlank() {
      final String rsqlQuery = "   ";

      final Resource resource = new Resource();
      final ResourceDto resourceDto = new ResourceDto();

      final List<Resource> resources = List.of(resource);
      final List<ResourceDto> resourceDtos = List.of(resourceDto);

      when(resourceRepository.findAll()).thenReturn(resources);
      when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

      final List<ResourceDto> result = resourceService.filterResources(rsqlQuery);

      assertEquals(resourceDtos, result);
      verify(resourceRepository).findAll();
      verify(resourceMapper).toDtos(resources);
      verifyNoMoreInteractions(resourceRepository, resourceMapper);
    }

    @Test
    @DisplayName("should throw bad request exception when rsql query is invalid")
    void shouldThrowBadRequestExceptionWhenRsqlQueryIsInvalid() {
      final String rsqlQuery = "resourceType=PDF";

      try (MockedStatic<RSQLJPASupport> mockedStatic =
          org.mockito.Mockito.mockStatic(RSQLJPASupport.class)) {
        mockedStatic
            .when(() -> RSQLJPASupport.toSpecification(rsqlQuery))
            .thenThrow(new RuntimeException());

        org.junit.jupiter.api.Assertions.assertThrows(
            com.ienrique.ressourceRelationnelle.exception.BadRequestException.class,
            () -> resourceService.filterResources(rsqlQuery));

        mockedStatic.verify(() -> RSQLJPASupport.toSpecification(rsqlQuery));
      }
    }
  }

  @Nested
  @DisplayName("add resource to favorite")
  class AddResourceToFavorite {

    @Test
    @DisplayName("should add resource to favorite")
    void shouldAddResourceToFavorite() {
      final UUID userId = UUID.randomUUID();
      final AppUser user = new AppUser();
      user.setAppUserId(userId);
      final UUID resourceId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      final UUID favoriteId = UUID.randomUUID();
      final Favorite savedFavorite = new Favorite();
      savedFavorite.setFavoriteId(favoriteId);
      savedFavorite.setAppUser(user);
      savedFavorite.setResource(resource);

      final FavoriteDto favoriteDto = new FavoriteDto(favoriteId, userId, resourceId);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(favoriteRepository.existsByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(false);
      when(favoriteRepository.save(any(Favorite.class))).thenReturn(savedFavorite);
      when(favoriteMapper.toDto(savedFavorite)).thenReturn(favoriteDto);

      final FavoriteDto result = resourceService.addResourceToFavorite(userId, resourceId);

      assertEquals(favoriteDto, result);

      verify(userRepository).findById(userId);
      verify(resourceRepository).findByResourceId(resourceId);
      verify(favoriteRepository).existsByAppUserAppUserIdAndResourceResourceId(userId, resourceId);
      verify(favoriteRepository).save(any(Favorite.class));
      verify(favoriteMapper).toDto(savedFavorite);
    }

    @Test
    @DisplayName("should throw when user not found")
    void shouldThrowWhenUserNotFound() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> resourceService.addResourceToFavorite(userId, resourceId));

      assertEquals("User not found", exception.getMessage());

      verify(userRepository).findById(userId);
      verify(resourceRepository, never()).findByResourceId(any());
      verify(favoriteRepository, never())
          .existsByAppUserAppUserIdAndResourceResourceId(any(), any());
      verify(favoriteRepository, never()).save(any());
      verify(favoriteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw when resource not found")
    void shouldThrowWhenResourceNotFound() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> resourceService.addResourceToFavorite(userId, resourceId));

      assertEquals("Resource not found", exception.getMessage());

      verify(userRepository).findById(userId);
      verify(resourceRepository).findByResourceId(resourceId);
      verify(favoriteRepository, never())
          .existsByAppUserAppUserIdAndResourceResourceId(any(), any());
      verify(favoriteRepository, never()).save(any());
      verify(favoriteMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw when resource is already in favorites")
    void shouldThrowWhenResourceIsAlreadyInFavorites() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(favoriteRepository.existsByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(true);

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> resourceService.addResourceToFavorite(userId, resourceId));

      assertEquals("Resource is already in favorites", exception.getMessage());

      verify(userRepository).findById(userId);
      verify(resourceRepository).findByResourceId(resourceId);
      verify(favoriteRepository).existsByAppUserAppUserIdAndResourceResourceId(userId, resourceId);
      verify(favoriteRepository, never()).save(any());
      verify(favoriteMapper, never()).toDto(any());
    }
  }

  @Nested
  @DisplayName("sort resources")
  class SortResources {

    @Test
    @DisplayName("should sort resources in ascending order")
    void shouldSortResourcesInAscendingOrder() {
      final List<Resource> resources = List.of(new Resource(), new Resource());
      final List<ResourceDto> resourceDtos = List.of(new ResourceDto(), new ResourceDto());

      when(resourceRepository.findAll(Sort.by("resourceCreatedAt").ascending()))
          .thenReturn(resources);
      when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

      final List<ResourceDto> result = resourceService.sortResources(true);

      assertEquals(resourceDtos, result);
      verify(resourceRepository).findAll(Sort.by("resourceCreatedAt").ascending());
      verify(resourceMapper).toDtos(resources);
    }

    @Test
    @DisplayName("should sort resources in descending order")
    void shouldSortResourcesInDescendingOrder() {
      final List<Resource> resources = List.of(new Resource(), new Resource());
      final List<ResourceDto> resourceDtos = List.of(new ResourceDto(), new ResourceDto());

      when(resourceRepository.findAll(Sort.by("resourceCreatedAt").descending()))
          .thenReturn(resources);
      when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

      final List<ResourceDto> result = resourceService.sortResources(false);

      assertEquals(resourceDtos, result);
      verify(resourceRepository).findAll(Sort.by("resourceCreatedAt").descending());
      verify(resourceMapper).toDtos(resources);
    }

    @Test
    @DisplayName("should return empty list when no resources found")
    void shouldReturnEmptyListWhenNoResourcesFound() {
      final List<Resource> resources = List.of();
      final List<ResourceDto> resourceDtos = List.of();

      when(resourceRepository.findAll(Sort.by("resourceCreatedAt").descending()))
          .thenReturn(resources);
      when(resourceMapper.toDtos(resources)).thenReturn(resourceDtos);

      final List<ResourceDto> result = resourceService.sortResources(false);

      assertEquals(resourceDtos, result);
      verify(resourceRepository).findAll(Sort.by("resourceCreatedAt").descending());
      verify(resourceMapper).toDtos(resources);
    }
  }
}

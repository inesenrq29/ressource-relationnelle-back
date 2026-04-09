package services;

import static org.junit.jupiter.api.Assertions.*;
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
import com.ienrique.ressourceRelationnelle.mapper.ResourceMapper;
import com.ienrique.ressourceRelationnelle.mapper.ShareResourceMapper;
import com.ienrique.ressourceRelationnelle.repository.*;
import com.ienrique.ressourceRelationnelle.service.ResourceServiceImpl;
import com.ienrique.ressourceRelationnelle.service.UserService;

import io.github.perplexhub.rsql.RSQLJPASupport;

@ExtendWith(MockitoExtension.class)
public class ResourceServiceTest {

  @Mock private ResourceRepository resourceRepository;
  @Mock private CategoryRepository categoryRepository;
  @Mock private AppUserRepository userRepository;
  @Mock private ProgressionRepository progressionRepository;
  @Mock private ShareResourceRepository shareResourceRepository;
  @Mock private FriendRepository friendRepository;
  @Mock private TagRepository tagRepository;
  @Mock private ResourceMapper resourceMapper;
  @Mock private ShareResourceMapper shareResourceMapper;
  @Mock private UserService userService;

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
    @DisplayName("should add resource to favorite when progression does not exist")
    void shouldAddResourceToFavoriteWhenProgressionDoesNotExist() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      resourceService.addResourceToFavorite(userId, resourceId);

      verify(progressionRepository)
          .save(
              argThat(
                  progression ->
                      progression.isFavorite()
                          && progression.getAppUser().equals(user)
                          && progression.getResource().equals(resource)));
    }

    @Test
    @DisplayName("should add resource to favorite when progression exists")
    void shouldAddResourceToFavoriteWhenProgressionExists() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setFavorite(false);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.addResourceToFavorite(userId, resourceId);

      assertTrue(progression.isFavorite());
      verify(progressionRepository).save(progression);
    }

    @Test
    @DisplayName("should throw exception when resource already in favorites")
    void shouldThrowWhenAlreadyFavorite() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setFavorite(true);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class,
          () -> resourceService.addResourceToFavorite(userId, resourceId));

      verify(progressionRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("remove resource from favorite")
  class RemoveResourceFromFavorite {

    @Test
    @DisplayName("should throw when progression is not found while removing favorite")
    void shouldThrowWhenProgressionNotFoundWhileRemovingFavorite() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> resourceService.removeResourceFromFavorite(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw when resource is not in favorites")
    void shouldThrowWhenResourceIsNotInFavorites() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setFavorite(false);
      progression.setSetAside(false);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class,
          () -> resourceService.removeResourceFromFavorite(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should delete progression when removing favorite and no status remains true")
    void shouldDeleteProgressionWhenRemovingFavoriteAndNoStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setFavorite(true);
      progression.setSetAside(false);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.removeResourceFromFavorite(userId, resourceId);

      assertFalse(progression.isFavorite());
      verify(progressionRepository).delete(progression);
      verify(progressionRepository, never()).save(any());
    }

    @Test
    @DisplayName("should save progression when removing favorite and another status remains true")
    void shouldSaveProgressionWhenRemovingFavoriteAndAnotherStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setFavorite(true);
      progression.setSetAside(true);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.removeResourceFromFavorite(userId, resourceId);

      assertFalse(progression.isFavorite());
      assertTrue(progression.isSetAside());
      verify(progressionRepository).save(progression);
      verify(progressionRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("mark resource as exploited")
  class MarkResourceAsExploited {

    @Test
    @DisplayName("should mark resource as exploited when progression does not exist")
    void shouldMarkResourceAsExploitedWhenProgressionDoesNotExist() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      resourceService.markResourceAsExploited(userId, resourceId);

      verify(progressionRepository)
          .save(
              argThat(
                  progression ->
                      progression.isExploited()
                          && progression.getAppUser().equals(user)
                          && progression.getResource().equals(resource)));
    }

    @Test
    @DisplayName("should mark resource as exploited when progression exists")
    void shouldMarkResourceAsExploitedWhenProgressionExists() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setExploited(false);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.markResourceAsExploited(userId, resourceId);

      assertTrue(progression.isExploited());
      verify(progressionRepository).save(progression);
    }

    @Test
    @DisplayName("should throw exception when resource already exploited")
    void shouldThrowWhenAlreadyExploited() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setExploited(true);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class,
          () -> resourceService.markResourceAsExploited(userId, resourceId));

      verify(progressionRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("mark resource as unexploited")
  class MarkResourceAsUnexploited {

    @Test
    @DisplayName("should throw when progression is not found while unmarking exploited resource")
    void shouldThrowWhenProgressionNotFoundWhileUnmarkingExploitedResource() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> resourceService.markResourceAsUnexploited(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw when resource is not exploited")
    void shouldThrowWhenResourceIsNotExploited() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setExploited(false);
      progression.setFavorite(false);
      progression.setSetAside(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class,
          () -> resourceService.markResourceAsUnexploited(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName(
        "should delete progression when unmarking exploited resource and no status remains true")
    void shouldDeleteProgressionWhenUnmarkingExploitedResourceAndNoStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setExploited(true);
      progression.setFavorite(false);
      progression.setSetAside(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.markResourceAsUnexploited(userId, resourceId);

      assertFalse(progression.isExploited());
      verify(progressionRepository).delete(progression);
      verify(progressionRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "should save progression when unmarking exploited resource and another status remains true")
    void shouldSaveProgressionWhenUnmarkingExploitedResourceAndAnotherStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setExploited(true);
      progression.setFavorite(true);
      progression.setSetAside(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.markResourceAsUnexploited(userId, resourceId);

      assertFalse(progression.isExploited());
      assertTrue(progression.isFavorite());
      verify(progressionRepository).save(progression);
      verify(progressionRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("set aside resource")
  class SetAsideResource {

    @Test
    @DisplayName("should set aside resource when progression does not exist")
    void shouldSetAsideResourceWhenProgressionDoesNotExist() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      resourceService.setAsideResource(userId, resourceId);

      verify(progressionRepository)
          .save(
              argThat(
                  progression ->
                      progression.isSetAside()
                          && progression.getAppUser().equals(user)
                          && progression.getResource().equals(resource)));
    }

    @Test
    @DisplayName("should set aside resource when progression exists")
    void shouldSetAsideResourceWhenProgressionExists() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setSetAside(false);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.setAsideResource(userId, resourceId);

      assertTrue(progression.isSetAside());
      verify(progressionRepository).save(progression);
    }

    @Test
    @DisplayName("should throw exception when resource already set aside")
    void shouldThrowWhenAlreadyExploited() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final AppUser user = new AppUser();
      final Resource resource = new Resource();

      final Progression progression = new Progression();
      progression.setSetAside(true);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class, () -> resourceService.setAsideResource(userId, resourceId));

      verify(progressionRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("unset aside resource")
  class UnsetAsideResource {

    @Test
    @DisplayName("should throw when progression is not found while unsetting aside resource")
    void shouldThrowWhenProgressionNotFoundWhileUnsettingAsideResource() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> resourceService.unsetAsideResource(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should throw when resource is not set aside")
    void shouldThrowWhenResourceIsNotSetAside() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setSetAside(false);
      progression.setFavorite(false);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      assertThrows(
          BadRequestException.class, () -> resourceService.unsetAsideResource(userId, resourceId));

      verify(progressionRepository, never()).save(any());
      verify(progressionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should delete progression when unsetting aside and no status remains true")
    void shouldDeleteProgressionWhenUnsettingAsideAndNoStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setSetAside(true);
      progression.setFavorite(false);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.unsetAsideResource(userId, resourceId);

      assertFalse(progression.isSetAside());
      verify(progressionRepository).delete(progression);
      verify(progressionRepository, never()).save(any());
    }

    @Test
    @DisplayName("should save progression when unsetting aside and another status remains true")
    void shouldSaveProgressionWhenUnsettingAsideAndAnotherStatusRemainsTrue() {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final Progression progression = new Progression();
      progression.setSetAside(true);
      progression.setFavorite(true);
      progression.setExploited(false);

      when(progressionRepository.findByAppUserAppUserIdAndResourceResourceId(userId, resourceId))
          .thenReturn(Optional.of(progression));

      resourceService.unsetAsideResource(userId, resourceId);

      assertFalse(progression.isSetAside());
      assertTrue(progression.isFavorite());
      verify(progressionRepository).save(progression);
      verify(progressionRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("get progression")
  class GetProgression {

    @Test
    @DisplayName("should return progression counts for a user")
    void shouldReturnProgressionCountsForUser() {
      final UUID userId = UUID.randomUUID();

      final AppUser user = new AppUser();

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(progressionRepository.countByAppUserAppUserIdAndFavoriteTrue(userId)).thenReturn(5L);
      when(progressionRepository.countByAppUserAppUserIdAndExploitedTrue(userId)).thenReturn(3L);
      when(progressionRepository.countByAppUserAppUserIdAndSetAsideTrue(userId)).thenReturn(2L);

      final ProgressionDto result = resourceService.getProgression(userId);

      assertEquals(5L, result.getFavoritesCount());
      assertEquals(3L, result.getExploitedCount());
      assertEquals(2L, result.getSetAsideCount());

      verify(userRepository).findById(userId);
      verify(progressionRepository).countByAppUserAppUserIdAndFavoriteTrue(userId);
      verify(progressionRepository).countByAppUserAppUserIdAndExploitedTrue(userId);
      verify(progressionRepository).countByAppUserAppUserIdAndSetAsideTrue(userId);
    }

    @Test
    @DisplayName("should throw when user not found for progression")
    void shouldThrowWhenUserNotFoundForProgression() {
      final UUID userId = UUID.randomUUID();

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> resourceService.getProgression(userId));

      verify(progressionRepository, never()).countByAppUserAppUserIdAndFavoriteTrue(any());
      verify(progressionRepository, never()).countByAppUserAppUserIdAndExploitedTrue(any());
      verify(progressionRepository, never()).countByAppUserAppUserIdAndSetAsideTrue(any());
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

  @Nested
  @DisplayName("share resource")
  class ShareResourceTest {

    @Test
    @DisplayName("should share resource when current user is request")
    void shouldShareResourceWhenCurrentUserIsRequester() {
      final UUID resourceId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendUserId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final AppUser sender = new AppUser();
      sender.setAppUserId(currentUserId);

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(friendUserId);

      final Friend friend = new Friend();
      friend.setRequesterUser(sender);
      friend.setReceiverUser(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final ShareResourceRequestDto request = new ShareResourceRequestDto();
      request.setMessage("hello");

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(friendRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sender));

      resourceService.shareResource(resourceId, friendId, request);

      verify(shareResourceRepository).save(any(ShareResource.class));
    }

    @Test
    @DisplayName("should throw resource not found")
    void shouldThrowWhenResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ShareResourceRequestDto request = new ShareResourceRequestDto();
      request.setMessage("hello");

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> resourceService.shareResource(resourceId, friendId, request));

      verify(shareResourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw friend not found")
    void shouldThrowWhenFriendNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final ShareResourceRequestDto request = new ShareResourceRequestDto();
      request.setMessage("hello");

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(friendRepository.findById(friendId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> resourceService.shareResource(resourceId, friendId, request));

      verify(shareResourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw bad request when user is not in friendship")
    void shouldThrowWhenUserNotInFriendship() {
      final UUID resourceId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final AppUser requester = new AppUser();
      requester.setAppUserId(UUID.randomUUID());

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(UUID.randomUUID());

      final Friend friend = new Friend();
      friend.setRequesterUser(requester);
      friend.setReceiverUser(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final AppUser sender = new AppUser();
      sender.setAppUserId(currentUserId);

      final ShareResourceRequestDto request = new ShareResourceRequestDto();
      request.setMessage("hello");

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(friendRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(userRepository.findById(currentUserId)).thenReturn(Optional.of(sender));

      assertThrows(
          BadRequestException.class,
          () -> resourceService.shareResource(resourceId, friendId, request));

      verify(shareResourceRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw current user not found")
    void shouldThrowWhenCurrentUserNotFound() {
      final UUID resourceId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(UUID.randomUUID());

      final AppUser sender = new AppUser();
      sender.setAppUserId(currentUserId);

      final Friend friend = new Friend();
      friend.setRequesterUser(sender);
      friend.setReceiverUser(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final ShareResourceRequestDto request = new ShareResourceRequestDto();
      request.setMessage("hello");

      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(friendRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> resourceService.shareResource(resourceId, friendId, request));

      verify(shareResourceRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("get shared resource")
  class GetSharedResource {

    @Test
    @DisplayName("should return shared resource when user is sender")
    void shouldReturnSharedResourceWhenUserIsSender() {
      final UUID shareId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AppUser sender = new AppUser();
      sender.setAppUserId(userId);

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(UUID.randomUUID());

      final ShareResource share = new ShareResource();
      share.setShareResourceId(shareId);
      share.setSender(sender);
      share.setReceiver(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final SharedResourceDto dto = new SharedResourceDto();

      when(shareResourceRepository.findById(shareId)).thenReturn(Optional.of(share));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(shareResourceMapper.toDto(share)).thenReturn(dto);

      final SharedResourceDto result = resourceService.getSharedResource(shareId);

      assertEquals(dto, result);
      verify(shareResourceMapper).toDto(share);
    }

    @Test
    @DisplayName("should return shared resource when user is receiver")
    void shouldReturnSharedResourceWhenUserIsReceiver() {
      final UUID shareId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AppUser sender = new AppUser();
      sender.setAppUserId(UUID.randomUUID());

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(userId);

      final ShareResource share = new ShareResource();
      share.setSender(sender);
      share.setReceiver(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final SharedResourceDto dto = new SharedResourceDto();

      when(shareResourceRepository.findById(shareId)).thenReturn(Optional.of(share));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(shareResourceMapper.toDto(share)).thenReturn(dto);

      final SharedResourceDto result = resourceService.getSharedResource(shareId);

      assertEquals(dto, result);
      verify(shareResourceMapper).toDto(share);
    }

    @Test
    @DisplayName("should throw share resource not found")
    void shouldThrowNotFoundExceptionWhenShareNotFound() {
      final UUID shareId = UUID.randomUUID();

      when(shareResourceRepository.findById(shareId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> resourceService.getSharedResource(shareId));

      verify(shareResourceMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw bad request when user is not sender and not receiver")
    void shouldThrowBadRequestWhenUserNotSenderNorReceiver() {
      final UUID shareId = UUID.randomUUID();

      final AppUser sender = new AppUser();
      sender.setAppUserId(UUID.randomUUID());

      final AppUser receiver = new AppUser();
      receiver.setAppUserId(UUID.randomUUID());

      final ShareResource share = new ShareResource();
      share.setSender(sender);
      share.setReceiver(receiver);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(UUID.randomUUID());

      when(shareResourceRepository.findById(shareId)).thenReturn(Optional.of(share));
      when(userService.getCurrentUser()).thenReturn(currentUser);

      assertThrows(BadRequestException.class, () -> resourceService.getSharedResource(shareId));

      verify(shareResourceMapper, never()).toDto(any());
    }
  }
}

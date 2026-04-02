package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.controller.ResourceController;
import com.ienrique.ressourceRelationnelle.dto.*;
import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;
import com.ienrique.ressourceRelationnelle.entity.ResourceType;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.service.ResourceService;

@WebMvcTest(ResourceController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class ResourceControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ResourceService resourceService;

  @Nested
  @DisplayName("get resource by id")
  class GetResourceById {

    @Test
    @DisplayName("should return resource with id")
    void shouldReturnResourceById() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      ResourceDto dto = new ResourceDto();
      dto.setResourceTitle("Test title");

      when(resourceService.getResourceById(resourceId)).thenReturn(dto);

      mockMvc
          .perform(get("/api/resources/{id}", resourceId).with(jwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.resourceTitle").value("Test title"));

      verify(resourceService).getResourceById(resourceId);
    }
  }

  @Nested
  @DisplayName("get resources")
  class GetResources {

    @Test
    @DisplayName("should return list of resources")
    void shouldReturnListOfResources() throws Exception {
      final ResourceDto dto1 = new ResourceDto();
      dto1.setResourceTitle("Title 1");

      final ResourceDto dto2 = new ResourceDto();
      dto2.setResourceTitle("Title 2");

      when(resourceService.getResources()).thenReturn(java.util.List.of(dto1, dto2));

      mockMvc
          .perform(get("/api/resources").with(jwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size()").value(2))
          .andExpect(jsonPath("$[0].resourceTitle").value("Title 1"))
          .andExpect(jsonPath("$[1].resourceTitle").value("Title 2"));

      verify(resourceService).getResources();
    }
  }

  @Nested
  @DisplayName("get restricted resources")
  class GetRestrictedResources {

    @Test
    @DisplayName("should return restricted resources")
    void shouldReturnRestrictedResources() throws Exception {
      final ResourceDto dto1 = new ResourceDto();
      dto1.setResourceTitle("Restricted 1");

      final ResourceDto dto2 = new ResourceDto();
      dto2.setResourceTitle("Restricted 2");

      when(resourceService.getRestrictedResources()).thenReturn(List.of(dto1, dto2));

      mockMvc
          .perform(get("/api/resources/restricted").with(jwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size()").value(2))
          .andExpect(jsonPath("$[0].resourceTitle").value("Restricted 1"))
          .andExpect(jsonPath("$[1].resourceTitle").value("Restricted 2"));

      verify(resourceService).getRestrictedResources();
    }

    @Test
    @DisplayName("should return empty list")
    void shouldReturnEmptyRestrictedList() throws Exception {
      when(resourceService.getRestrictedResources()).thenReturn(List.of());

      mockMvc
          .perform(get("/api/resources/restricted").with(jwt()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.size()").value(0));

      verify(resourceService).getRestrictedResources();
    }
  }

  @Nested
  @DisplayName("create resource")
  class CreateResource {

    @Test
    @DisplayName("should create resource and return 201")
    void shouldCreateResource() throws Exception {
      final CreateResourceDto request = new CreateResourceDto();
      request.setResourceTitle("New resource");
      request.setResourceDescription("Description");
      request.setResourceIsUsed(true);
      request.setStatus(ResourceStatus.DRAFT);
      request.setResourceType(ResourceType.VIDEO);
      request.setCategoryId(UUID.randomUUID());
      request.setTags(List.of("tag1", "tag2"));

      final ResourceDto response = new ResourceDto();
      response.setResourceTitle("New resource");

      when(resourceService.createResource(any(CreateResourceDto.class))).thenReturn(response);

      mockMvc
          .perform(
              post("/api/resources")
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.resourceTitle").value("New resource"));

      verify(resourceService).createResource(any(CreateResourceDto.class));
    }
  }

  @Nested
  @DisplayName("update resource")
  class UpdateResource {
    @Test
    @DisplayName("should update resource and return 204")
    void shouldUpdateResource() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      final UpdateResourceDto request = new UpdateResourceDto();
      request.setResourceTitle("Updated title");
      request.setResourceDescription("Updated description");
      request.setResourceIsUsed(true);
      request.setResourceType(ResourceType.GAME);
      request.setCategoryId(UUID.randomUUID());
      request.setTags(List.of("tag1", "tag2"));

      mockMvc
          .perform(
              put("/api/resources/{id}", resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(resourceService).updateResource(eq(resourceId), any(UpdateResourceDto.class));
    }

    @Test
    @DisplayName("should return 400 when request is invalid")
    void shouldReturn400WhenInvalidRequest() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      final UpdateResourceDto request = new UpdateResourceDto();

      mockMvc
          .perform(
              put("/api/resources/{id}", resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(resourceService, never()).updateResource(any(), any());
    }
  }

  @Nested
  @DisplayName("delete resource")
  class DeleteResource {

    @Test
    @DisplayName("should delete resource and return 204")
    void shouldDeleteResource() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      mockMvc
          .perform(delete("/api/resources/{id}", resourceId).with(jwt()).with(csrf()))
          .andExpect(status().isNoContent());

      verify(resourceService).deleteResource(resourceId);
    }

    @Test
    @DisplayName("should return 404 when resource not found")
    void shouldReturn404WhenDeletingUnknownResource() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      doThrow(new NotFoundException("Resource not found"))
          .when(resourceService)
          .deleteResource(resourceId);

      mockMvc
          .perform(delete("/api/resources/{id}", resourceId).with(jwt()).with(csrf()))
          .andExpect(status().isNotFound());

      verify(resourceService).deleteResource(resourceId);
    }
  }

  @Nested
  @DisplayName("submit resource for validation")
  class SubmitResourceForValidation {

    @Test
    @DisplayName("should submit resource for validation and return 204")
    void shouldSubmitResourceForValidation() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      mockMvc
          .perform(patch("/api/resources/{id}/submit", resourceId).with(jwt()).with(csrf()))
          .andExpect(status().isNoContent());

      verify(resourceService).submitForValidation(resourceId);
    }

    @Test
    @DisplayName("should return 404 when resource not found")
    void shouldReturn404WhenSubmittingUnknownResource() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      doThrow(new NotFoundException("Resource not found"))
          .when(resourceService)
          .submitForValidation(resourceId);

      mockMvc
          .perform(patch("/api/resources/{id}/submit", resourceId).with(jwt()).with(csrf()))
          .andExpect(status().isNotFound());

      verify(resourceService).submitForValidation(resourceId);
    }
  }

  @Nested
  @DisplayName("update resource status")
  class UpdateResourceStatus {

    @Test
    @DisplayName("should update status and return 204")
    void shouldUpdateResourceStatus() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      final UpdateResourceStatusDto request = new UpdateResourceStatusDto();
      request.setStatus(ResourceStatus.PUBLISHED);

      mockMvc
          .perform(
              put("/api/resources/{id}/status", resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());

      verify(resourceService)
          .updateResourceStatus(eq(resourceId), any(UpdateResourceStatusDto.class));
    }

    @Test
    @DisplayName("should return 400 when status is missing")
    void shouldReturn400WhenStatusIsMissing() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      final UpdateResourceStatusDto request = new UpdateResourceStatusDto();

      mockMvc
          .perform(
              put("/api/resources/{id}/status", resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isBadRequest());

      verify(resourceService, never()).updateResourceStatus(any(), any());
    }

    @Test
    @DisplayName("should return 404 when resource not found")
    void shouldReturn404WhenResourceNotFound() throws Exception {
      final UUID resourceId = UUID.randomUUID();

      final UpdateResourceStatusDto request = new UpdateResourceStatusDto();
      request.setStatus(ResourceStatus.PUBLISHED);

      doThrow(new NotFoundException("Resource not found"))
          .when(resourceService)
          .updateResourceStatus(eq(resourceId), any(UpdateResourceStatusDto.class));

      mockMvc
          .perform(
              put("/api/resources/{id}/status", resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("add resource to favorite")
  class AddResourceToFavorite {

    @Test
    @DisplayName("should add resource to favorites")
    void shouldAddResourceToFavorite() throws Exception {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();
      final FavoriteDto favoriteDto = new FavoriteDto();

      when(resourceService.addResourceToFavorite(eq(userId), eq(resourceId)))
          .thenReturn(favoriteDto);

      mockMvc
          .perform(
              post("/api/resources/{userId}/favorite/{resourceId}", userId, resourceId)
                  .with(jwt())
                  .with(csrf())
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isCreated());

      verify(resourceService).addResourceToFavorite(eq(userId), eq(resourceId));
    }
  }

  @Nested
  @DisplayName("sort resources")
  class SortResources {

    @Test
    @DisplayName("should return sorted resources in ascending order")
    void shouldReturnSortedResourcesInAscendingOrder() throws Exception {
      final ResourceDto resource1 = new ResourceDto();
      final ResourceDto resource2 = new ResourceDto();

      final List<ResourceDto> resources = List.of(resource1, resource2);

      when(resourceService.sortResources(true)).thenReturn(resources);

      mockMvc
          .perform(
              get("/api/resources/sorted")
                  .with(jwt())
                  .param("isAscending", "true")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());

      verify(resourceService).sortResources(true);
    }

    @Test
    @DisplayName("should return sorted resources in descending order")
    void shouldReturnSortedResourcesInDescendingOrder() throws Exception {
      final ResourceDto resource1 = new ResourceDto();
      final ResourceDto resource2 = new ResourceDto();

      final List<ResourceDto> resources = List.of(resource1, resource2);

      when(resourceService.sortResources(false)).thenReturn(resources);

      mockMvc
          .perform(
              get("/api/resources/sorted")
                  .with(jwt())
                  .param("isAscending", "false")
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());

      verify(resourceService).sortResources(false);
    }
  }
}

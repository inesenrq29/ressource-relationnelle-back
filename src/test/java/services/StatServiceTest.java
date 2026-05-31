package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ienrique.ressourceRelationnelle.dto.StatsDashboardDto;
import com.ienrique.ressourceRelationnelle.dto.StatsFilterDto;
import com.ienrique.ressourceRelationnelle.entity.Category;
import com.ienrique.ressourceRelationnelle.entity.Progression;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.entity.ResourceType;
import com.ienrique.ressourceRelationnelle.repository.ProgressionRepository;
import com.ienrique.ressourceRelationnelle.repository.ResourceRepository;
import com.ienrique.ressourceRelationnelle.service.StatServiceImpl;

@ExtendWith(MockitoExtension.class)
public class StatServiceTest {

  @Mock private ProgressionRepository progressionRepository;
  @Mock private ResourceRepository resourceRepository;

  @InjectMocks private StatServiceImpl statService;

  @Nested
  @DisplayName("get stats")
  class GetStats {

    @Test
    @DisplayName("Should return global statistics successfully")
    void shouldReturnGlobalStatisticsSuccessfully() {
      final Category familyCategory = new Category();
      familyCategory.setCategoryId(UUID.randomUUID());
      familyCategory.setName("Famille");

      final Category friendsCategory = new Category();
      friendsCategory.setCategoryId(UUID.randomUUID());
      friendsCategory.setName("Amis");

      final Resource resource1 = new Resource();
      resource1.setResourceId(UUID.randomUUID());
      resource1.setCategory(familyCategory);
      resource1.setResourceType(ResourceType.ARTICLE);

      final Resource resource2 = new Resource();
      resource2.setResourceId(UUID.randomUUID());
      resource2.setCategory(familyCategory);
      resource2.setResourceType(ResourceType.VIDEO);

      final Resource resource3 = new Resource();
      resource3.setResourceId(UUID.randomUUID());
      resource3.setCategory(friendsCategory);
      resource3.setResourceType(ResourceType.ARTICLE);

      final Progression progression1 = new Progression();
      progression1.setResource(resource1);
      progression1.setExploited(true);

      final Progression progression2 = new Progression();
      progression2.setResource(resource2);
      progression2.setExploited(true);

      final Progression progression3 = new Progression();
      progression3.setResource(resource1);
      progression3.setExploited(true);

      final Progression progression4 = new Progression();
      progression4.setResource(resource3);
      progression4.setExploited(false);

      when(resourceRepository.count()).thenReturn(3L);
      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2, resource3));
      when(progressionRepository.findAll())
          .thenReturn(List.of(progression1, progression2, progression3, progression4));

      final StatsDashboardDto result = statService.getStats();

      assertNotNull(result);
      assertEquals(3L, result.getTotalResourcesCount());
      assertEquals(2L, result.getExploitedResourcesCount());

      assertEquals(2L, result.getResourcesByCategory().get("Famille"));
      assertEquals(1L, result.getResourcesByCategory().get("Amis"));

      assertEquals(2L, result.getResourcesByType().get("ARTICLE"));
      assertEquals(1L, result.getResourcesByType().get("VIDEO"));

      verify(resourceRepository).count();
      verify(resourceRepository, times(2)).findAll();
      verify(progressionRepository).findAll();
    }

    @Test
    @DisplayName("Should ignore progression without resource")
    void shouldIgnoreProgressionWithoutResource() {
      when(resourceRepository.count()).thenReturn(0L);
      when(resourceRepository.findAll()).thenReturn(List.of());

      final Progression progression = new Progression();
      progression.setExploited(true);
      progression.setResource(null);

      when(progressionRepository.findAll()).thenReturn(List.of(progression));

      final StatsDashboardDto result = statService.getStats();

      assertNotNull(result);
      assertEquals(0L, result.getTotalResourcesCount());
      assertEquals(0L, result.getExploitedResourcesCount());
      assertTrue(result.getResourcesByCategory().isEmpty());
      assertTrue(result.getResourcesByType().isEmpty());

      verify(resourceRepository).count();
      verify(resourceRepository, times(2)).findAll();
      verify(progressionRepository).findAll();
    }
  }

  @Nested
  @DisplayName("get filtered stats")
  class GetFilteredStats {

    @Test
    @DisplayName("Should filter statistics by resource type")
    void shouldFilterStatisticsByResourceType() {
      final Category familyCategory = new Category();
      familyCategory.setCategoryId(UUID.randomUUID());
      familyCategory.setName("Famille");

      final Category friendsCategory = new Category();
      friendsCategory.setCategoryId(UUID.randomUUID());
      friendsCategory.setName("Amis");

      final Resource resource1 = new Resource();
      resource1.setResourceId(UUID.randomUUID());
      resource1.setCategory(familyCategory);
      resource1.setResourceType(ResourceType.ARTICLE);
      resource1.setResourceCreatedAt(Instant.parse("2026-01-10T10:00:00Z"));

      final Resource resource2 = new Resource();
      resource2.setResourceId(UUID.randomUUID());
      resource2.setCategory(familyCategory);
      resource2.setResourceType(ResourceType.VIDEO);
      resource2.setResourceCreatedAt(Instant.parse("2026-02-10T10:00:00Z"));

      final Resource resource3 = new Resource();
      resource3.setResourceId(UUID.randomUUID());
      resource3.setCategory(friendsCategory);
      resource3.setResourceType(ResourceType.ARTICLE);
      resource3.setResourceCreatedAt(Instant.parse("2026-03-10T10:00:00Z"));

      final Progression progression1 = new Progression();
      progression1.setResource(resource1);
      progression1.setExploited(true);

      final Progression progression2 = new Progression();
      progression2.setResource(resource2);
      progression2.setExploited(true);

      final Progression progression3 = new Progression();
      progression3.setResource(resource3);
      progression3.setExploited(true);

      final StatsFilterDto filter = new StatsFilterDto();
      filter.setResourceType(ResourceType.ARTICLE);

      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2, resource3));
      when(progressionRepository.findAll())
          .thenReturn(List.of(progression1, progression2, progression3));

      final StatsDashboardDto result = statService.getStats(filter);

      assertNotNull(result);
      assertEquals(2, result.getTotalResourcesCount());
      assertEquals(2L, result.getExploitedResourcesCount());
      assertEquals(1L, result.getResourcesByCategory().get("Famille"));
      assertEquals(1L, result.getResourcesByCategory().get("Amis"));
      assertEquals(2L, result.getResourcesByType().get("ARTICLE"));
      assertFalse(result.getResourcesByType().containsKey("VIDEO"));

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }

    @Test
    @DisplayName("Should filter statistics by category")
    void shouldFilterStatisticsByCategory() {
      final UUID categoryId = UUID.randomUUID();

      final Category familyCategory = new Category();
      familyCategory.setCategoryId(categoryId);
      familyCategory.setName("Famille");

      final Category friendsCategory = new Category();
      friendsCategory.setCategoryId(UUID.randomUUID());
      friendsCategory.setName("Amis");

      final Resource resource1 = new Resource();
      resource1.setResourceId(UUID.randomUUID());
      resource1.setCategory(familyCategory);
      resource1.setResourceType(ResourceType.ARTICLE);

      final Resource resource2 = new Resource();
      resource2.setResourceId(UUID.randomUUID());
      resource2.setCategory(familyCategory);
      resource2.setResourceType(ResourceType.VIDEO);

      final Resource resource3 = new Resource();
      resource3.setResourceId(UUID.randomUUID());
      resource3.setCategory(friendsCategory);
      resource3.setResourceType(ResourceType.ARTICLE);

      final Progression progression1 = new Progression();
      progression1.setResource(resource1);
      progression1.setExploited(true);

      final Progression progression2 = new Progression();
      progression2.setResource(resource2);
      progression2.setExploited(true);

      final Progression progression3 = new Progression();
      progression3.setResource(resource3);
      progression3.setExploited(true);

      final StatsFilterDto filter = new StatsFilterDto();
      filter.setCategoryId(categoryId);

      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2, resource3));
      when(progressionRepository.findAll())
          .thenReturn(List.of(progression1, progression2, progression3));

      final StatsDashboardDto result = statService.getStats(filter);

      assertNotNull(result);
      assertEquals(2, result.getTotalResourcesCount());
      assertEquals(2L, result.getExploitedResourcesCount());
      assertEquals(Map.of("Famille", 2L), result.getResourcesByCategory());
      assertEquals(1L, result.getResourcesByType().get("ARTICLE"));
      assertEquals(1L, result.getResourcesByType().get("VIDEO"));

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }

    @Test
    @DisplayName("Should filter statistics by date range")
    void shouldFilterStatisticsByDateRange() {
      final Category familyCategory = new Category();
      familyCategory.setCategoryId(UUID.randomUUID());
      familyCategory.setName("Famille");

      final Resource resource1 = new Resource();
      resource1.setResourceId(UUID.randomUUID());
      resource1.setCategory(familyCategory);
      resource1.setResourceType(ResourceType.ARTICLE);
      resource1.setResourceCreatedAt(Instant.parse("2026-01-10T10:00:00Z"));

      final Resource resource2 = new Resource();
      resource2.setResourceId(UUID.randomUUID());
      resource2.setCategory(familyCategory);
      resource2.setResourceType(ResourceType.VIDEO);
      resource2.setResourceCreatedAt(Instant.parse("2026-02-10T10:00:00Z"));

      final Resource resource3 = new Resource();
      resource3.setResourceId(UUID.randomUUID());
      resource3.setCategory(familyCategory);
      resource3.setResourceType(ResourceType.ARTICLE);
      resource3.setResourceCreatedAt(Instant.parse("2026-03-10T10:00:00Z"));

      final Progression progression1 = new Progression();
      progression1.setResource(resource1);
      progression1.setExploited(true);

      final Progression progression2 = new Progression();
      progression2.setResource(resource2);
      progression2.setExploited(true);

      final Progression progression3 = new Progression();
      progression3.setResource(resource3);
      progression3.setExploited(true);

      final StatsFilterDto filter = new StatsFilterDto();
      filter.setStartDate(Instant.parse("2026-02-01T00:00:00Z"));
      filter.setEndDate(Instant.parse("2026-02-28T23:59:59Z"));

      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2, resource3));
      when(progressionRepository.findAll())
          .thenReturn(List.of(progression1, progression2, progression3));

      final StatsDashboardDto result = statService.getStats(filter);

      assertNotNull(result);
      assertEquals(1, result.getTotalResourcesCount());
      assertEquals(1L, result.getExploitedResourcesCount());
      assertEquals(Map.of("Famille", 1L), result.getResourcesByCategory());
      assertEquals(Map.of("VIDEO", 1L), result.getResourcesByType());

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }

    @Test
    @DisplayName("Should return unfiltered statistics when filter is null")
    void shouldReturnUnfilteredStatisticsWhenFilterIsNull() {
      final Category category = new Category();
      category.setCategoryId(UUID.randomUUID());
      category.setName("Famille");

      final Resource resource = new Resource();
      resource.setResourceId(UUID.randomUUID());
      resource.setCategory(category);
      resource.setResourceType(ResourceType.ARTICLE);

      final Progression progression = new Progression();
      progression.setResource(resource);
      progression.setExploited(true);

      when(resourceRepository.findAll()).thenReturn(List.of(resource));
      when(progressionRepository.findAll()).thenReturn(List.of(progression));

      final StatsDashboardDto result = statService.getStats(null);

      assertNotNull(result);
      assertEquals(1, result.getTotalResourcesCount());
      assertEquals(1L, result.getExploitedResourcesCount());
      assertEquals(1L, result.getResourcesByCategory().get("Famille"));
      assertEquals(1L, result.getResourcesByType().get("ARTICLE"));

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }
  }

  @Nested
  @DisplayName("export stats")
  class ExportStats {

    @Test
    @DisplayName("Should export statistics as CSV successfully")
    void shouldExportStatisticsAsCsvSuccessfully() {
      final Category familyCategory = new Category();
      familyCategory.setCategoryId(UUID.randomUUID());
      familyCategory.setName("Famille");

      final Category friendsCategory = new Category();
      friendsCategory.setCategoryId(UUID.randomUUID());
      friendsCategory.setName("Amis");

      final Resource resource1 = new Resource();
      resource1.setResourceId(UUID.randomUUID());
      resource1.setCategory(familyCategory);
      resource1.setResourceType(ResourceType.ARTICLE);

      final Resource resource2 = new Resource();
      resource2.setResourceId(UUID.randomUUID());
      resource2.setCategory(familyCategory);
      resource2.setResourceType(ResourceType.VIDEO);

      final Resource resource3 = new Resource();
      resource3.setResourceId(UUID.randomUUID());
      resource3.setCategory(friendsCategory);
      resource3.setResourceType(ResourceType.ARTICLE);

      final Progression progression = new Progression();
      progression.setResource(resource1);
      progression.setExploited(true);

      final StatsFilterDto filter = new StatsFilterDto();

      when(resourceRepository.findAll()).thenReturn(List.of(resource1, resource2, resource3));
      when(progressionRepository.findAll()).thenReturn(List.of(progression));

      final byte[] result = statService.exportStats(filter);
      final String csv = new String(result, StandardCharsets.UTF_8);

      assertNotNull(result);
      assertFalse(csv.isBlank());
      assertTrue(csv.contains("Indicateur,Valeur"));
      assertTrue(csv.contains("Nombre total de ressources,3"));
      assertTrue(csv.contains("Nombre de ressources exploitees,1"));
      assertTrue(csv.contains("Ressources par categorie"));
      assertTrue(csv.contains("Categorie,Nombre"));
      assertTrue(csv.contains("Famille,2"));
      assertTrue(csv.contains("Amis,1"));
      assertTrue(csv.contains("Ressources par type"));
      assertTrue(csv.contains("Type,Nombre"));
      assertTrue(csv.contains("ARTICLE,2"));
      assertTrue(csv.contains("VIDEO,1"));

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }

    @Test
    @DisplayName("Should export empty statistics as CSV successfully")
    void shouldExportEmptyStatisticsAsCsvSuccessfully() {
      final StatsFilterDto filter = new StatsFilterDto();

      when(resourceRepository.findAll()).thenReturn(List.of());
      when(progressionRepository.findAll()).thenReturn(List.of());

      final byte[] result = statService.exportStats(filter);
      final String csv = new String(result, StandardCharsets.UTF_8);

      assertNotNull(result);
      assertFalse(csv.isBlank());
      assertTrue(csv.contains("Indicateur,Valeur"));
      assertTrue(csv.contains("Nombre total de ressources,0"));
      assertTrue(csv.contains("Nombre de ressources exploitees,0"));
      assertTrue(csv.contains("Ressources par categorie"));
      assertTrue(csv.contains("Ressources par type"));

      verify(resourceRepository).findAll();
      verify(progressionRepository).findAll();
    }
  }
}

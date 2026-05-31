package com.ienrique.ressourceRelationnelle.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ienrique.ressourceRelationnelle.dto.StatsDashboardDto;
import com.ienrique.ressourceRelationnelle.dto.StatsFilterDto;
import com.ienrique.ressourceRelationnelle.entity.Progression;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.repository.ProgressionRepository;
import com.ienrique.ressourceRelationnelle.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

  private final ResourceRepository resourceRepository;
  private final ProgressionRepository progressionRepository;

  @Override
  public StatsDashboardDto getStats() {

    final StatsDashboardDto dashboardDto = new StatsDashboardDto();
    dashboardDto.setTotalResourcesCount(resourceRepository.count());

    final long exploitedResources =
        progressionRepository.findAll().stream()
            .filter(Progression::isExploited)
            .filter(progression -> progression.getResource() != null)
            .map(progression -> progression.getResource().getResourceId())
            .distinct()
            .count();

    final Map<String, Long> categoryMap =
        resourceRepository.findAll().stream()
            .filter(resource -> resource.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    resource -> resource.getCategory().getName(), Collectors.counting()));
    final Map<String, Long> typeMap =
        resourceRepository.findAll().stream()
            .filter(resource -> resource.getResourceType() != null)
            .collect(
                Collectors.groupingBy(
                    resource -> resource.getResourceType().name(), Collectors.counting()));

    dashboardDto.setExploitedResourcesCount(exploitedResources);
    dashboardDto.setResourcesByCategory(categoryMap);
    dashboardDto.setResourcesByType(typeMap);

    return dashboardDto;
  }

  @Override
  public StatsDashboardDto getStats(StatsFilterDto filter) {
    // récup toutes les ressources
    List<Resource> resources = resourceRepository.findAll();
    // garder celles qui correspondent aux filtres
    if (filter != null && filter.getResourceType() != null) {
      resources =
          resources.stream()
              .filter(resource -> resource.getResourceType() != null)
              .filter(resource -> resource.getResourceType().equals(filter.getResourceType()))
              .toList();
    }

    if (filter != null && filter.getCategoryId() != null) {
      resources =
          resources.stream()
              .filter(resource -> resource.getCategory() != null)
              .filter(
                  resource -> resource.getCategory().getCategoryId().equals(filter.getCategoryId()))
              .toList();
    }

    if (filter != null && filter.getStartDate() != null) {
      resources =
          resources.stream()
              .filter(resource -> resource.getResourceCreatedAt() != null)
              .filter(resource -> !resource.getResourceCreatedAt().isBefore(filter.getStartDate()))
              .toList();
    }

    if (filter != null && filter.getEndDate() != null) {
      resources =
          resources.stream()
              .filter(resource -> resource.getResourceCreatedAt() != null)
              .filter(resource -> !resource.getResourceCreatedAt().isAfter(filter.getEndDate()))
              .toList();
    }

    final Map<String, Long> categoryMap =
        resources.stream()
            .filter(resource -> resource.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    resource -> resource.getCategory().getName(), Collectors.counting()));
    final Map<String, Long> typeMap =
        resources.stream()
            .filter(resource -> resource.getResourceType() != null)
            .collect(
                Collectors.groupingBy(
                    resource -> resource.getResourceType().name(), Collectors.counting()));

    // récupérer les ids des ressources filtrées
    final Set<UUID> filteredResourceIds =
        resources.stream().map(Resource::getResourceId).collect(Collectors.toSet());

    final long exploitedResources =
        progressionRepository.findAll().stream()
            .filter(progression -> progression.getResource() != null)
            .filter(Progression::isExploited) // ne garde que les ressources exploitées
            .map(
                progression ->
                    progression
                        .getResource()
                        .getResourceId()) // on récupère l'id de la ressource associée à une
            // progression
            .filter(
                filteredResourceIds
                    ::contains) // on garde les ressources présentes dans la liste filtrée
            .distinct() // on enlève les doublons
            .count(); // on compte

    final StatsDashboardDto dashboard = new StatsDashboardDto();
    dashboard.setTotalResourcesCount(resources.size());
    dashboard.setResourcesByCategory(categoryMap);
    dashboard.setResourcesByType(typeMap);
    dashboard.setExploitedResourcesCount(exploitedResources);

    return dashboard;
  }

  @Override
  public byte[] exportStats(StatsFilterDto filter) {
    final StatsDashboardDto stats = getStats(filter);

    final StringBuilder csv = new StringBuilder();

    csv.append("Indicateur,Valeur\n");
    csv.append("Nombre total de ressources,").append(stats.getTotalResourcesCount()).append("\n");
    csv.append("Nombre de ressources exploitees,")
        .append(stats.getExploitedResourcesCount())
        .append("\n");
    csv.append("\n");

    csv.append("Ressources par categorie\n");
    csv.append("Categorie,Nombre\n");
    for (Map.Entry<String, Long> entry : stats.getResourcesByCategory().entrySet()) {
      csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
    }
    csv.append("\n");

    csv.append("Ressources par type\n");
    csv.append("Type,Nombre\n");
    for (Map.Entry<String, Long> entry : stats.getResourcesByType().entrySet()) {
      csv.append(entry.getKey()).append(",").append(entry.getValue()).append("\n");
    }

    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }
}

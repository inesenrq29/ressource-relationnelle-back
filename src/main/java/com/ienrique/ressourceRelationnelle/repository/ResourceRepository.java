package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.entity.ResourceStatus;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

  Optional<Resource> findByResourceTitle(String resourceTitle);

  List<Resource> findByCategoryCategoryId(UUID categoryId);

  List<Resource> findByTagsTagId(UUID tagId);

  List<Resource> findByStatus(
      ResourceStatus status); // avec en status par exemple RESTRICTED pour ressource restreinte
}

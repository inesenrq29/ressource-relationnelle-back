package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Resource;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, UUID> {

  Resource findByResourceTitle(String resourceTitle);

  List<Resource> findByCategoryCategoryId(UUID categoryId);

  List<Resource> findByTagsTagId(UUID tagId);
}

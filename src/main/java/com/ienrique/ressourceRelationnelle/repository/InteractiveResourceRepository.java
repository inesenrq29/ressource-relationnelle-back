package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.InteractiveResource;

@Repository
public interface InteractiveResourceRepository extends JpaRepository<InteractiveResource, UUID> {

  Optional<InteractiveResource> findByResourceResourceId(UUID resourceId);
}

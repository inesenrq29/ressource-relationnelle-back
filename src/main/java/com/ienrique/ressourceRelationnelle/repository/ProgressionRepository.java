package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ienrique.ressourceRelationnelle.entity.Progression;

public interface ProgressionRepository extends JpaRepository<Progression, UUID> {

  Optional<Progression> findByAppUserAppUserIdAndResourceResourceId(UUID userId, UUID resourceId);

  List<Progression> findAllByAppUserAppUserId(UUID userId);

  long countByAppUserAppUserIdAndFavoriteTrue(UUID userId);

  long countByAppUserAppUserIdAndSetAsideTrue(UUID userId);

  long countByAppUserAppUserIdAndExploitedTrue(UUID userId);

  long countByExploitedTrue();
}

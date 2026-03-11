package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Favorite;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

  List<Favorite> findByAppUserAppUserId(UUID appUserId);

  Optional<Favorite> findByAppUserAppUserIdAndResourceResourceId(UUID appUserId, UUID resourceId);

  boolean existsByAppUserAppUserIdAndResourceResourceId(UUID appUserId, UUID resourceId);
}

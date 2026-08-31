package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.RefreshToken;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
  Optional<RefreshToken> findByHashedToken(String hashedToken);

  List<RefreshToken> findByAppUser_AppUserIdAndRevokedFalse(UUID appUserId);

  void deleteByAppUser_AppUserId(UUID appUserId);
}

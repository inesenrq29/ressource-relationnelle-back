package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.PasswordResetToken;
import com.ienrique.ressourceRelationnelle.entity.TokenType;

@Repository
public interface PasswordRepository extends JpaRepository<PasswordResetToken, UUID> {
  Optional<PasswordResetToken> findByTokenValueAndType(String tokenValue, TokenType type);

  void deleteByUser_AppUserIdAndType(UUID appUserId, TokenType type);

  void deleteByAppUser_AppUserId(UUID appUserId);
}

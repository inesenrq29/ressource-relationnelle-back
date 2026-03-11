package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PasswordResetToken")
@Getter
@Setter
public class PasswordResetToken {

  @Id
  @GeneratedValue
  @Column(name = "tokenId", nullable = false, updatable = false)
  private UUID tokenId;

  @Column(name = "tokenValue", nullable = false, unique = true)
  private String tokenValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private TokenType type;

  @Column(name = "expiresAt", nullable = false)
  private Instant expiresAt;

  @Column(name = "used", nullable = false)
  private boolean used = false;

  @Column(name = "createdAt", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser user;
}

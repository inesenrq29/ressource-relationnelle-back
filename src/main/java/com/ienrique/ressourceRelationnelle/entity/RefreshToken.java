package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "refresh_token")
@Getter
@Setter
public class RefreshToken {

  @Id
  @GeneratedValue
  @Column(name = "refresh_token_id", nullable = false, updatable = false)
  private UUID refreshTokenId;

  @Column(name = "hashed_token", nullable = false, unique = true)
  private String hashedToken;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Lob
  @Column(name = "user_agent")
  private String userAgent;

  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUser appUser;
}

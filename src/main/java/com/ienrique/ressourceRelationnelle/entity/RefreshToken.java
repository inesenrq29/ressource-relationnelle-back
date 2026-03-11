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
@Table(name = "RefreshToken")
@Getter
@Setter
public class RefreshToken {

  @Id
  @GeneratedValue
  @Column(name = "refreshTokenId", nullable = false, updatable = false)
  private UUID refreshTokenId;

  @Column(name = "hashedToken", nullable = false, unique = true)
  private String hashedToken;

  @Column(name = "createdAt", nullable = false, insertable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expiresAt", nullable = false)
  private Instant expiresAt;

  @Column(name = "ipAddress", length = 45)
  private String ipAddress;

  @Lob
  @Column(name = "userAgent")
  private String userAgent;

  @Column(name = "revoked", nullable = false)
  private boolean revoked = false;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser appUser;
}

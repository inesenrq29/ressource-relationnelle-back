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
@Table(name = "AppUser")
@Getter
@Setter
public class AppUser {

  @Id
  @GeneratedValue
  @Column(
      name = "appUserId",
      nullable = false,
      updatable = false,
      length = 36,
      columnDefinition = "CHAR(36)")
  private UUID appUserId;

  @Column(name = "mail", nullable = false, unique = true)
  private String mail;

  @Column(name = "pseudo", nullable = false, unique = true, length = 100)
  private String pseudo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "roleId", nullable = false)
  private Role role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private AccountStatus status;

  @Column(name = "appUserIsActive", nullable = false)
  private boolean appUserIsActive = true;

  @Column(name = "areTermsAccepted", nullable = false)
  private boolean areTermsAccepted;

  @Column(name = "isPrivacyPolicyAccepted", nullable = false)
  private boolean isPrivacyPolicyAccepted;

  @Column(name = "hashedPassword", nullable = false)
  private String hashedPassword;

  @Column(name = "previousPassword")
  private String previousPassword;

  @Column(name = "createdAt", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updatedAt")
  private Instant updatedAt;

  @Column(name = "lastConnectionAt")
  private Instant lastConnectionAt;
}

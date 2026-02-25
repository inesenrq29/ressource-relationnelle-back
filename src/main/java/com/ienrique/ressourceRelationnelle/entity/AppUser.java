package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Getter
@Setter
public class AppUser {

  @Id
  @GeneratedValue
  @Column(name = "app_user_id", nullable = false, updatable = false)
  private UUID appUserId;

  @Column(name = "mail", nullable = false, unique = true)
  private String mail;

  @Column(name = "pseudo", nullable = false, unique = true, length = 100)
  private String pseudo;

  @Column(name = "app_user_is_active", nullable = false)
  private boolean appUserIsActive = true;

  @Column(name = "hashed_password", nullable = false)
  private String hashedPassword;

  @Column(name = "previous_password")
  private String previousPassword;

  @Column(name = "last_connection_at")
  private Instant lastConnectionAt;
}

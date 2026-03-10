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
@Table(name = "AppUser")
@Getter
@Setter
public class AppUser {

  @Id
  @GeneratedValue
  @Column(name = "appUserId", nullable = false, updatable = false, length = 36)
  private UUID appUserId;

  @Column(name = "mail", nullable = false, unique = true)
  private String mail;

  @Column(name = "pseudo", nullable = false, unique = true, length = 100)
  private String pseudo;

  @Column(name = "appUserIsActive", nullable = false)
  private boolean appUserIsActive = true;

  @Column(name = "hashedPassword", nullable = false)
  private String hashedPassword;

  @Column(name = "previousPassword")
  private String previousPassword;

  @Column(name = "lastConnectionAt")
  private Instant lastConnectionAt;
}

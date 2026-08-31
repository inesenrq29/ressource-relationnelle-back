package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "LoginAttempt")
@Getter
@Setter
public class LoginAttempt {

  @Id
  @GeneratedValue
  @Column(name = "loginAttemptId", nullable = false, updatable = false)
  private UUID loginAttemptId;

  @Column(name = "attemptedEmail", nullable = false)
  private String attemptedEmail;

  @Column(name = "attemptAt", nullable = false, insertable = false, updatable = false)
  private Instant attemptAt;

  @Column(name = "success", nullable = false)
  private boolean success = false;

  @Column(name = "ipAddress", length = 45)
  private String ipAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId")
  private AppUser appUser;
}

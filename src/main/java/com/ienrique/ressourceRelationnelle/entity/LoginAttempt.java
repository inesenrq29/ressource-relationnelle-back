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
@Table(name = "login_attempt")
@Getter
@Setter
public class LoginAttempt {

  @Id
  @GeneratedValue
  @Column(name = "login_attempt_id", nullable = false, updatable = false)
  private UUID loginAttemptId;

  @Column(name = "attempted_email", nullable = false)
  private String attemptedEmail;

  @Column(name = "attempt_at", nullable = false)
  private Instant attemptAt;

  @Column(name = "success", nullable = false)
  private boolean success = false;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUser appUser;
}

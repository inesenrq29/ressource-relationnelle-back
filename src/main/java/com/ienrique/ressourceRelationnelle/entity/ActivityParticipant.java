package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ActivityParticipant")
@Getter
@Setter
public class ActivityParticipant {

  @Id
  @GeneratedValue
  @Column(name = "activityParticipantId", nullable = false, updatable = false)
  private UUID activityParticipantId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activitySessionId", nullable = false)
  private ActivitySession activitySession;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser appUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private ParticipantRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ParticipantStatus status;

  @Column(name = "invitedAt")
  private Instant invitedAt;

  @Column(name = "joinedAt")
  private Instant joinedAt;
}

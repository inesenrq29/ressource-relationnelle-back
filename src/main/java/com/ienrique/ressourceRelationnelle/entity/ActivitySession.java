package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ActivitySession")
@Getter
@Setter
public class ActivitySession {

  @Id
  @GeneratedValue
  @Column(name = "activitySessionId", nullable = false, updatable = false)
  private UUID activitySessionId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interactiveResourceId", nullable = false)
  private InteractiveResource interactiveResource;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ActivitySessionStatus status;

  @Column(name = "startedAt", nullable = false)
  private Instant startedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "createdById", nullable = false)
  private AppUser createdBy;
}

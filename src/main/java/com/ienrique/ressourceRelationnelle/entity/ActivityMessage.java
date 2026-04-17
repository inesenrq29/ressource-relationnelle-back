package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ActivityMessage")
@Getter
@Setter
public class ActivityMessage {

  @Id
  @GeneratedValue
  @Column(name = "activityMessageId", nullable = false, updatable = false)
  private UUID activityMessageId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activitySessionId", nullable = false)
  private ActivitySession activitySession;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser sender;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "sentAt", nullable = false)
  private Instant sentAt;
}

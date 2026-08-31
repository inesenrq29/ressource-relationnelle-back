package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ShareResource")
@Getter
@Setter
public class ShareResource {

  @Id
  @GeneratedValue
  @Column(
      name = "shareResourceId",
      nullable = false,
      updatable = false,
      length = 36,
      columnDefinition = "CHAR(36)")
  private UUID shareResourceId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "senderUserId", nullable = false)
  private AppUser sender;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "receiverUserId", nullable = false)
  private AppUser receiver;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;

  @Column(name = "message", columnDefinition = "TEXT")
  private String message;

  @Column(name = "sharedAt", nullable = false)
  private Instant sharedAt;
}

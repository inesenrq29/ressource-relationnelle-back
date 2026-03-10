package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Resource")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Resource {

  @Id
  @GeneratedValue
  @Column(name = "resourceId", nullable = false, updatable = false)
  private UUID resourceId;

  @Column(name = "resourceIsActive", nullable = false)
  private boolean resourceIsActive = true;

  @Column(name = "resourceIsUsed", nullable = false)
  private boolean resourceIsUsed;

  @Column(name = "resourceTitle", nullable = false)
  private String resourceTitle;

  @Lob
  @Column(name = "resourceDescription")
  private String resourceDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ResourceStatus status;

  @Column(name = "resourceCreatedAt", nullable = false, insertable = false, updatable = false)
  private Instant resourceCreatedAt;
}

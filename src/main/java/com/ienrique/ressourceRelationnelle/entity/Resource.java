package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import org.w3c.dom.Text;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "resource")
@Getter
@Setter
public abstract class Resource {

  @Id
  @GeneratedValue
  @Column(name = "resource_id", nullable = false, updatable = false)
  private UUID resourceId;

  @Column(name = "resource_is_active", nullable = false)
  private boolean resourceIsActive = true;

  @Column(name = "resource_is_used", nullable = false)
  private boolean resourceIsUsed;

  @Column(name = "resource_title", nullable = false)
  private String resourceTitle;

  @Column(name = "resource_description")
  private Text resourceDescription;

  @Column(name = "status", nullable = false)
  private String status;

  @Column(name = "resource_created_at", nullable = false, updatable = false)
  private Instant resourceCreatedAt;
}

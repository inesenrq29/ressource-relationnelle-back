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
@Table(name = "history_state_resource")
@Getter
@Setter
public class HistoryStateResource {

  @Id
  @GeneratedValue
  @Column(name = "history_status_resource_id", nullable = false, updatable = false)
  private UUID historyStatusResourceId;

  @Column(name = "modification_date", nullable = false)
  private Instant modificationDate;

  @Column(name = "old_resource", nullable = false, length = 100)
  private String oldResource;

  @Column(name = "comment")
  private String comment;

  @Column(name = "new_resource", nullable = false, length = 100)
  private String newResource;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resource_id", nullable = false)
  private Resource resource;
}

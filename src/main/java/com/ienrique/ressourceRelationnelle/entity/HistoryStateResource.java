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
@Table(name = "HistoryStateResource")
@Getter
@Setter
public class HistoryStateResource {

  @Id
  @GeneratedValue
  @Column(name = "historyStatusResourceId", nullable = false, updatable = false)
  private UUID historyStatusResourceId;

  @Column(name = "modificationDate", nullable = false, insertable = false, updatable = false)
  private Instant modificationDate;

  @Column(name = "oldResource", nullable = false, length = 100)
  private String oldResource;

  @Column(name = "comment")
  private String comment;

  @Column(name = "newResource", nullable = false, length = 100)
  private String newResource;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;
}

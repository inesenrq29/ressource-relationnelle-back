package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Progression")
@Getter
@Setter
public class Progression {

  @Id
  @GeneratedValue
  @Column(name = "progressionId", nullable = false, updatable = false)
  private UUID progressionId;

  @Column(name = "exploited", nullable = false)
  private boolean exploited = false;

  @Column(name = "favorite", nullable = false)
  private boolean favorite = false;

  @Column(name = "setAside", nullable = false)
  private boolean setAside = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser appUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;
}

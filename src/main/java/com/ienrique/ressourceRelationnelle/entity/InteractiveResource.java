package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "InteractiveResource")
public class InteractiveResource {

  @Id
  @GeneratedValue
  @Column(name = "interactiveResourceId", nullable = false, updatable = false)
  private UUID interactiveResourceId;

  @Enumerated(EnumType.STRING)
  @Column(name = "activityType", nullable = false)
  private ActivityType activityType;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", unique = true)
  private Resource resource;
}

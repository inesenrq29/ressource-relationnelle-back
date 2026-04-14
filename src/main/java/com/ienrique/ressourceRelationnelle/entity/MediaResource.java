package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "MediaResource")
public class MediaResource {

  @Id
  @GeneratedValue
  @Column(name = "mediaResourceId", nullable = false, updatable = false)
  private UUID mediaResourceId;

  @Column(name = "mediaWeight", nullable = false)
  private Long mediaWeight;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", unique = true)
  private Resource resource;
}

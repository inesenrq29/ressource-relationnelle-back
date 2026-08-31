package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Video")
@Getter
@Setter
public class Video {

  @Id
  @GeneratedValue
  @Column(name = "videoId", nullable = false, updatable = false)
  private UUID videoId;

  @Column(name = "videoWeight", nullable = false)
  private Long videoWeight;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", unique = true)
  private Resource resource;
}

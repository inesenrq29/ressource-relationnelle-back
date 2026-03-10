package com.ienrique.ressourceRelationnelle.entity;

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
@Table(name = "Game")
@Getter
@Setter
public class Game {

  @Id
  @GeneratedValue
  @Column(name = "gameId", nullable = false, updatable = false)
  private UUID gameId;

  @Column(name = "gameWeight", nullable = false)
  private Long gameWeight;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId")
  private Resource resource;
}

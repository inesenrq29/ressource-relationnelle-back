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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "Favorite",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_favorite_user_resource",
          columnNames = {"appUserId", "resourceId"})
    })
@Getter
@Setter
public class Favorite {

  @Id
  @GeneratedValue
  @Column(name = "favoriteId", nullable = false, updatable = false)
  private UUID favoriteId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser appUser;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;
}

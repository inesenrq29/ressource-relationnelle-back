package com.ienrique.ressourceRelationnelle.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Tag")
@Getter
@Setter
public class Tag {

  @Id
  @GeneratedValue
  @Column(name = "tagId", nullable = false, updatable = false)
  private UUID tagId;

  @Column(name = "wording", nullable = false, length = 140, unique = true)
  private String wording;

  @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
  private Set<Resource> resources = new HashSet<>();
}

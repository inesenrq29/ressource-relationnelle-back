package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Resource")
@Getter
@Setter
public abstract class Resource {

  @Id
  @GeneratedValue
  @Column(name = "resourceId", nullable = false, updatable = false)
  private UUID resourceId;

  @Column(name = "resourceIsActive", nullable = false)
  private boolean resourceIsActive = true; // ressource activée ou désactivée

  @Column(name = "resourceIsUsed", nullable = false)
  private boolean resourceIsUsed; // ressource exploitée ou non

  @Column(name = "resourceTitle", nullable = false)
  private String resourceTitle;

  @Lob
  @Column(name = "resourceDescription")
  private String resourceDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ResourceStatus status;

  @Column(name = "resourceCreatedAt", nullable = false, insertable = false, updatable = false)
  private Instant resourceCreatedAt;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "Resource_Tag",
      joinColumns = @JoinColumn(name = "resourceId"),
      inverseJoinColumns = @JoinColumn(name = "tagId"))
  private Set<Tag> tags = new HashSet<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "categoryId")
  private Category category;
}

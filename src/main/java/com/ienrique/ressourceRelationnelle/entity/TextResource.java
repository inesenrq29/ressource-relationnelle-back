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
@Table(name = "TextResource")
public class TextResource {

  @Id
  @GeneratedValue
  @Column(name = "textResourceId", nullable = false, updatable = false)
  private UUID textResourceId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", unique = true)
  private Resource resource;
}

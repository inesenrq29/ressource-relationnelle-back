package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Comments")
@Getter
@Setter
public class Comments {

  @Id
  @GeneratedValue
  @Column(name = "commentsId", nullable = false, updatable = false)
  private UUID commentsId;

  @Column(name = "publicationDate", nullable = false)
  private Instant publicationDate;

  @Column(
      name = "modificationCommentsDate",
      nullable = false,
      insertable = false,
      updatable = false)
  private Instant modificationCommentsDate;

  @Column(name = "author", nullable = false, length = 100)
  private String author;

  @Column(name = "titleComments", length = 100)
  private String titleComments;

  @Lob
  @Column(name = "commentsContent", nullable = false)
  private String commentsContent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;
}

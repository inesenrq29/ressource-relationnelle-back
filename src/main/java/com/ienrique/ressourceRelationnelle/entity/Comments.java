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
@Table(name = "comments")
@Getter
@Setter
public class Comments {

  @Id
  @GeneratedValue
  @Column(name = "comments_id", nullable = false, updatable = false)
  private UUID commentsId;

  @Column(name = "publication_date", nullable = false)
  private Instant publicationDate;

  @Column(name = "modification_comments_date", insertable = false, updatable = false)
  private Instant modificationCommentsDate;

  @Column(name = "author", nullable = false, length = 100)
  private String author;

  @Column(name = "title_comments", length = 100)
  private String titleComments;

  @Lob
  @Column(name = "comments_content", nullable = false)
  private String commentsContent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resource_id", nullable = false)
  private Resource resource;
}

package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parentCommentId") // permet de stocker l'id du commentaire auquel on répond
  private Comments parentComment;

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

  @Column(name = "commentsContent", nullable = false, columnDefinition = "TEXT")
  private String commentsContent;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CommentStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resourceId", nullable = false)
  private Resource resource;

  @Column(name = "moderationReason")
  private String moderationReason;
}

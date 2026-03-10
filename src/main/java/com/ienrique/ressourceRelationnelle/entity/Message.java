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
@Table(name = "Message")
@Getter
@Setter
public class Message {

  @Id
  @GeneratedValue
  @Column(name = "messageId", nullable = false, updatable = false)
  private UUID messageId;

  @Column(name = "titleType", nullable = false, length = 100)
  private String titleType;

  @Lob
  @Column(name = "messageDescription")
  private String messageDescription;

  @Column(name = "messageCreatedAt", nullable = false, insertable = false, updatable = false)
  private Instant messageCreatedAt;

  @Column(name = "messageUpdatedAt", nullable = false, insertable = false, updatable = false)
  private Instant messageUpdatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "appUserId", nullable = false)
  private AppUser appUser;
}

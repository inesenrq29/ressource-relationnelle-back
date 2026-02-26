package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(name = "message")
@Getter
@Setter
public class Message {

  @Id
  @GeneratedValue
  @Column(name = "message_id", nullable = false, updatable = false)
  private UUID messageId;

  @Column(name = "title_type", nullable = false, length = 100)
  private String titleType;

  @Lob
  @Column(name = "message_description")
  private String messageDescription;

  @CreationTimestamp
  @Column(name = "message_created_at", nullable = false, updatable = false)
  private Instant messageCreatedAt;

  @UpdateTimestamp
  @Column(name = "message_updated_at", nullable = false, insertable = false)
  private Instant messageUpdatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_user_id", nullable = false)
  private AppUser user;
}

package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
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
@Table(name = "friend")
@Getter
@Setter
public class Friend {

  @Id
  @GeneratedValue
  @Column(name = "friend_id", nullable = false, updatable = false)
  private UUID friendId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requester_user_id", nullable = false)
  private AppUser requesterUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "receiver_user_id", nullable = false)
  private AppUser receiverUser;

  @Column(name = "status", nullable = false)
  private String status; // TODO: mettre en enum

  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;
}

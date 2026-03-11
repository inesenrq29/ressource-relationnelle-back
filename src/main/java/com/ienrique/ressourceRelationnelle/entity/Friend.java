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
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Friend")
@Getter
@Setter
public class Friend {

  @Id
  @GeneratedValue
  @Column(name = "friendId", nullable = false, updatable = false)
  private UUID friendId;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requesterUserId", nullable = false)
  private AppUser requesterUser;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "friendReceiverUserId", nullable = false)
  private AppUser friendReceiverUserId;
}

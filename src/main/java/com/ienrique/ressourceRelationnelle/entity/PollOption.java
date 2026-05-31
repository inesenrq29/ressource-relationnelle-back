package com.ienrique.ressourceRelationnelle.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PollOption")
@Getter
@Setter
public class PollOption {

  @Id
  @GeneratedValue
  @Column(name = "pollOptionId", nullable = false, updatable = false)
  private UUID pollOptionId;

  @Column(name = "optionLabel", nullable = false)
  private String optionLabel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pollId", nullable = false)
  private Poll poll;
}

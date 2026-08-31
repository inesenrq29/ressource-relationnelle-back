package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PollParticipantAnswer")
@Getter
@Setter
public class PollParticipantAnswer {

  @Id
  @GeneratedValue
  @Column(name = "pollParticipantAnswerId", nullable = false, updatable = false)
  private UUID pollParticipantAnswerId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "activityParticipantId", nullable = false)
  private ActivityParticipant activityParticipant;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pollOptionId", nullable = false)
  private PollOption pollOption;

  @Column(name = "answeredAt")
  private Instant answeredAt;
}

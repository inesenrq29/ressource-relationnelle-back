package com.ienrique.ressourceRelationnelle.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Poll")
@Getter
@Setter
public class Poll {

  @Id
  @GeneratedValue
  @Column(name = "pollId", nullable = false, updatable = false)
  private UUID pollId;

  @Column(name = "createdAt", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "question", nullable = false, columnDefinition = "TEXT")
  private String question;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "interactiveResourceId", nullable = false, unique = true)
  private InteractiveResource interactiveResource;

  @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PollOption> options = new ArrayList<>();
}

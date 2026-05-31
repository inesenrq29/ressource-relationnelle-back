package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Poll;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {

  Optional<Poll> findByInteractiveResourceInteractiveResourceId(UUID interactiveResourceId);
}

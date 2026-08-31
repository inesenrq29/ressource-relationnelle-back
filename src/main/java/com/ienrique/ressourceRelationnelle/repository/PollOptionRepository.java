package com.ienrique.ressourceRelationnelle.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.PollOption;

@Repository
public interface PollOptionRepository extends JpaRepository<PollOption, UUID> {}

package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.ActivityParticipant;
import com.ienrique.ressourceRelationnelle.entity.ActivitySession;
import com.ienrique.ressourceRelationnelle.entity.AppUser;

@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {

  Optional<ActivityParticipant> findByActivitySessionActivitySessionIdAndAppUserAppUserId(
      UUID activitySessionId, UUID appUserId);

  boolean existsByActivitySessionAndAppUser(ActivitySession activitySession, AppUser appUser);
}

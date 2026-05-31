package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.ActivityMessage;

@Repository
public interface ActivityMessageRepository extends JpaRepository<ActivityMessage, UUID> {

  List<ActivityMessage> findByActivitySessionActivitySessionId(UUID activitySessionId);

  List<ActivityMessage> findByActivitySessionActivitySessionIdOrderBySentAtAsc(
      UUID activitySessionId);
}

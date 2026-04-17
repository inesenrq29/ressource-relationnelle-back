package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Friend;

@Repository
public interface FriendRepository extends JpaRepository<Friend, UUID> {

  List<Friend> findByRequesterUserAppUserId(UUID requesterUserId);

  List<Friend> findByReceiverUserAppUserId(UUID receiverUserId);

  Optional<Friend> findByRequesterUserAppUserIdAndReceiverUserAppUserId(
      UUID requesterUserId, UUID receiverUserId);

  boolean existsByRequesterUserAppUserIdAndReceiverUserAppUserId(
      UUID requesterUserId, UUID receiverUserId);
}

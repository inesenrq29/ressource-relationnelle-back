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

  List<Friend> findByFriendReceiverUserIdAppUserId(UUID friendReceiverUserId);

  Optional<Friend> findByRequesterUserAppUserIdAndFriendReceiverUserIdAppUserId(
      UUID requesterUserId, UUID friendReceiverUserId);
}

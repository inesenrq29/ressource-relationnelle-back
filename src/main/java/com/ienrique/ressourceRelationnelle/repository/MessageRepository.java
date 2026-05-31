package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

  List<Message> findByAppUserAppUserId(UUID appUserId);

  List<Message> findByTitleType(String titleType);

  List<Message> findByAppUserAppUserIdAndTitleType(UUID appUserId, String titleType);
}

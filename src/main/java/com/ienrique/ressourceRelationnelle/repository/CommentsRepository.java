package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Comments;

@Repository
public interface CommentsRepository extends JpaRepository<Comments, UUID> {

  List<Comments> findByResourceResourceId(UUID resourceId);

  List<Comments> findByAuthor(String author);
}

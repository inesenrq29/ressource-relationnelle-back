package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Tag;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

  Optional<Tag> findByWording(String wording);

  boolean existsByWording(String wording);
}

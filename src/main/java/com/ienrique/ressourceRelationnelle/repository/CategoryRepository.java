package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

  Optional<Category> findByName(String name);

  boolean existsByName(String name);
}

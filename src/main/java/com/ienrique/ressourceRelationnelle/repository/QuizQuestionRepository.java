package com.ienrique.ressourceRelationnelle.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.QuizQuestion;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {}

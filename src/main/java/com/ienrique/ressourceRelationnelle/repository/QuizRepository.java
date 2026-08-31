package com.ienrique.ressourceRelationnelle.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {

  Optional<Quiz> findByInteractiveResourceInteractiveResourceId(UUID interactiveResourceId);

  Optional<Quiz> findByQuestionsQuizQuestionId(UUID quizQuestionId);
}

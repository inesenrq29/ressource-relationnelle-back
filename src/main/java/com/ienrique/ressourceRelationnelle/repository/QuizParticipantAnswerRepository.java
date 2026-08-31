package com.ienrique.ressourceRelationnelle.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.ActivityParticipant;
import com.ienrique.ressourceRelationnelle.entity.QuizParticipantAnswer;
import com.ienrique.ressourceRelationnelle.entity.QuizQuestion;

@Repository
public interface QuizParticipantAnswerRepository
    extends JpaRepository<QuizParticipantAnswer, UUID> {

  boolean existsByActivityParticipantAndQuizQuestion(
      ActivityParticipant participant, QuizQuestion question);

  List<QuizParticipantAnswer> findByActivityParticipant(ActivityParticipant participant);
}

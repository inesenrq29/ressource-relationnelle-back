package com.ienrique.ressourceRelationnelle.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ienrique.ressourceRelationnelle.entity.ActivityParticipant;
import com.ienrique.ressourceRelationnelle.entity.Poll;
import com.ienrique.ressourceRelationnelle.entity.PollParticipantAnswer;

@Repository
public interface PollParticipantAnswerRepository
    extends JpaRepository<PollParticipantAnswer, UUID> {

  boolean existsByActivityParticipantAndPollOptionPoll(ActivityParticipant participant, Poll poll);
}

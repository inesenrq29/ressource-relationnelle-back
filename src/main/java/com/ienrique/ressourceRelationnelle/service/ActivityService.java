package com.ienrique.ressourceRelationnelle.service;

import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.activity.*;

public interface ActivityService {

  ActivityResponseDto startActivity(UUID resourceId);

  QuizDto createQuiz(CreateQuizDto createQuizDto);

  PollDto createPoll(CreatePollDto createPollDto);

  CheckQuizAnswerResponseDto checkQuizAnswer(CheckQuizAnswerDto request);
}

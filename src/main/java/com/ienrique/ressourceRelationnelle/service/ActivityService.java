package com.ienrique.ressourceRelationnelle.service;

import java.util.List;
import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.ActivityMessageDto;
import com.ienrique.ressourceRelationnelle.dto.AnswerPollDto;
import com.ienrique.ressourceRelationnelle.dto.SendMessageDto;
import com.ienrique.ressourceRelationnelle.dto.activity.*;

public interface ActivityService {

  ActivityResponseDto startActivity(UUID resourceId);

  QuizDto createQuiz(CreateQuizDto createQuizDto);

  PollDto createPoll(CreatePollDto createPollDto);

  CheckQuizAnswerResponseDto answerQuizQuestion(CheckQuizAnswerDto checkQuizAnswerDto);

  void answerPollOption(AnswerPollDto answerPollDto);

  int getQuizScore(UUID activitySessionId);

  void inviteParticipant(UUID activitySessionId, UUID friendId);

  void acceptInvitation(UUID activitySessionId);

  void declineInvitation(UUID activitySessionId);

  void sendMessage(UUID activitySessionId, SendMessageDto content);

  List<ActivityMessageDto> getMessages(UUID activitySessionId);
}

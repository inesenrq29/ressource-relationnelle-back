package com.ienrique.ressourceRelationnelle.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.ienrique.ressourceRelationnelle.dto.ActivityMessageDto;
import com.ienrique.ressourceRelationnelle.dto.AnswerPollDto;
import com.ienrique.ressourceRelationnelle.dto.SendMessageDto;
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.service.ActivityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  @PostMapping("/start/{resourceId}")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<ActivityResponseDto> startActivity(final @PathVariable UUID resourceId) {
    final ActivityResponseDto response = activityService.startActivity(resourceId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/quiz")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<QuizDto> createQuiz(final @Valid @RequestBody CreateQuizDto request) {
    final QuizDto response = activityService.createQuiz(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/poll")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<PollDto> createPoll(final @Valid @RequestBody CreatePollDto request) {
    final PollDto response = activityService.createPoll(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/quiz/answer")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<CheckQuizAnswerResponseDto> checkQuizAnswer(
      @Valid @RequestBody final CheckQuizAnswerDto request) {
    final CheckQuizAnswerResponseDto response = activityService.answerQuizQuestion(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/poll/answer")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> checkPollOptionAnswer(
      @Valid @RequestBody final AnswerPollDto request) {
    activityService.answerPollOption(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/quiz/score/{activitySessionId}")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Integer> getQuizScore(@PathVariable final UUID activitySessionId) {
    final int score = activityService.getQuizScore(activitySessionId);
    return ResponseEntity.ok(score);
  }

  @PostMapping("/{activitySessionId}/participants/invite/{friendId}")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> inviteParticipant(
      @PathVariable final UUID activitySessionId, @PathVariable final UUID friendId) {
    activityService.inviteParticipant(activitySessionId, friendId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{activitySessionId}/participants/accept")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> acceptInvitation(@PathVariable final UUID activitySessionId) {
    activityService.acceptInvitation(activitySessionId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{activitySessionId}/participants/decline")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> declineInvitation(@PathVariable final UUID activitySessionId) {
    activityService.declineInvitation(activitySessionId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/messages/{activitySessionId}")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> sendMessage(
      @PathVariable final UUID activitySessionId,
      @Valid @RequestBody final SendMessageDto content) {
    activityService.sendMessage(activitySessionId, content);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/messages/{activitySessionId}")
  @PreAuthorize("hasAnyRole('CITIZEN', 'MODERATOR', 'ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<List<ActivityMessageDto>> getMessages(
      @PathVariable final UUID activitySessionId) {
    return ResponseEntity.ok(activityService.getMessages(activitySessionId));
  }
}

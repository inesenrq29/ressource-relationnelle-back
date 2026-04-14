package com.ienrique.ressourceRelationnelle.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<ActivityResponseDto> startActivity(final @PathVariable UUID resourceId) {
    final ActivityResponseDto response = activityService.startActivity(resourceId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/quiz")
  public ResponseEntity<QuizDto> createQuiz(final @Valid @RequestBody CreateQuizDto request) {
    final QuizDto response = activityService.createQuiz(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/poll")
  public ResponseEntity<PollDto> createPoll(final @Valid @RequestBody CreatePollDto request) {
    final PollDto response = activityService.createPoll(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/quiz/check")
  public ResponseEntity<CheckQuizAnswerResponseDto> checkQuizAnswer(
      @Valid @RequestBody final CheckQuizAnswerDto request) {
    final CheckQuizAnswerResponseDto response = activityService.checkQuizAnswer(request);
    return ResponseEntity.ok(response);
  }
}

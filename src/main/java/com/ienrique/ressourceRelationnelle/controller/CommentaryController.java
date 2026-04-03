package com.ienrique.ressourceRelationnelle.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ienrique.ressourceRelationnelle.dto.*;
import com.ienrique.ressourceRelationnelle.service.CommentaryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentaryController {

  private final CommentaryService commentaryService;

  @PostMapping("/{userId}/resources/{resourceId}")
  public ResponseEntity<CommentDto> addComment(
      final @PathVariable UUID userId,
      final @PathVariable UUID resourceId,
      final @Valid @RequestBody CreateCommentDto requestDto) {

    final CommentDto response = commentaryService.addComment(userId, resourceId, requestDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{userId}/resources/{resourceId}/{commentsId}/reply")
  public ResponseEntity<CommentDto> respondToComment(
      final @PathVariable UUID userId,
      final @PathVariable UUID resourceId,
      final @PathVariable UUID commentsId,
      final @Valid @RequestBody CreateCommentDto requestDto) {

    final CommentDto response =
        commentaryService.respondToComment(userId, resourceId, commentsId, requestDto);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{commentsId}/moderate")
  public ResponseEntity<CommentDto> moderateComment(
      final @PathVariable UUID commentsId,
      final @Valid @RequestBody ModerateCommentDto requestDto) {

    final CommentDto response = commentaryService.moderateComment(commentsId, requestDto);

    return ResponseEntity.ok(response);
  }
}

package com.ienrique.ressourceRelationnelle.service;

import java.util.UUID;

import com.ienrique.ressourceRelationnelle.dto.CommentDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCommentDto;
import com.ienrique.ressourceRelationnelle.dto.ModerateCommentDto;

public interface CommentaryService {

  CommentDto addComment(UUID userId, UUID resourceId, CreateCommentDto createCommentDto);

  CommentDto respondToComment(
      UUID userId, UUID resourceId, UUID commentsId, CreateCommentDto createCommentDto);

  CommentDto moderateComment(UUID commentsId, ModerateCommentDto moderateCommentDto);
}

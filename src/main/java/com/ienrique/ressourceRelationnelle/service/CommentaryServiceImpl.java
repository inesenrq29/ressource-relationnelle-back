package com.ienrique.ressourceRelationnelle.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ienrique.ressourceRelationnelle.dto.CommentDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCommentDto;
import com.ienrique.ressourceRelationnelle.dto.ModerateCommentDto;
import com.ienrique.ressourceRelationnelle.entity.AppUser;
import com.ienrique.ressourceRelationnelle.entity.CommentStatus;
import com.ienrique.ressourceRelationnelle.entity.Comments;
import com.ienrique.ressourceRelationnelle.entity.Resource;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.CommentsMapper;
import com.ienrique.ressourceRelationnelle.repository.AppUserRepository;
import com.ienrique.ressourceRelationnelle.repository.CommentsRepository;
import com.ienrique.ressourceRelationnelle.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentaryServiceImpl implements CommentaryService {

  private final CommentsRepository commentsRepository;
  private final ResourceRepository resourceRepository;
  private final AppUserRepository userRepository;
  private final CommentsMapper commentsMapper;

  @Override
  public List<CommentDto> getCommentsByResourceId(UUID resourceId) {
    return commentsMapper.toDtos(commentsRepository.findByResourceResourceId(resourceId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<CommentDto> getCommentsForModeration(CommentStatus status) {
    return commentsRepository.findAll().stream()
        .filter(comment -> status == null || comment.getStatus() == status)
        .map(commentsMapper::toDto)
        .toList();
  }

  @Override
  public CommentDto addComment(UUID userId, UUID resourceId, CreateCommentDto createCommentDto) {
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    if (createCommentDto.getCommentsContent() == null
        || createCommentDto.getCommentsContent().isBlank()) {
      throw new BadRequestException("Comment content is required");
    }

    final Comments comment = new Comments();

    comment.setTitleComments(createCommentDto.getTitleComments());
    comment.setCommentsContent(createCommentDto.getCommentsContent());
    comment.setResource(resource);
    comment.setAuthor(user.getPseudo());
    comment.setPublicationDate(Instant.now());
    comment.setStatus(CommentStatus.PENDING);

    final Comments savedComment = commentsRepository.save(comment);

    return commentsMapper.toDto(savedComment);
  }

  @Override
  public CommentDto respondToComment(
      UUID userId, UUID resourceId, UUID commentsId, CreateCommentDto createCommentDto) {

    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));

    final Comments parentComment =
        commentsRepository
            .findByCommentsId(commentsId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));

    if (!parentComment.getResource().getResourceId().equals(resourceId)) {
      throw new BadRequestException("Comment does not belong to this resource");
    }

    if (createCommentDto.getCommentsContent() == null
        || createCommentDto.getCommentsContent().isBlank()) {
      throw new BadRequestException("Comment content is required");
    }

    final Comments response = new Comments();

    response.setParentComment(parentComment);
    response.setTitleComments(createCommentDto.getTitleComments());
    response.setCommentsContent(createCommentDto.getCommentsContent());
    response.setResource(resource);
    response.setAuthor(user.getPseudo());
    response.setPublicationDate(Instant.now());
    response.setStatus(CommentStatus.PENDING);

    final Comments savedResponse = commentsRepository.save(response);

    return commentsMapper.toDto(savedResponse);
  }

  @Override
  @Transactional
  public CommentDto moderateComment(UUID commentsId, ModerateCommentDto moderateCommentDto) {
    final Comments comment =
        commentsRepository
            .findByCommentsId(commentsId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));

    if (moderateCommentDto.getStatus() == null) {
      throw new BadRequestException("Comment status is required");
    }

    if (moderateCommentDto.getStatus() == CommentStatus.PENDING) {
      throw new BadRequestException("Invalid moderation status");
    }

    if (moderateCommentDto.getStatus() == CommentStatus.REJECTED) {
      if (moderateCommentDto.getModerationReason() == null
          || moderateCommentDto.getModerationReason().isBlank()) {
        throw new BadRequestException("Moderation reason is required");
      }

      comment.setModerationReason(moderateCommentDto.getModerationReason());
    } else {
      comment.setModerationReason(null);
    }

    comment.setStatus(moderateCommentDto.getStatus());

    final Comments savedComment = commentsRepository.save(comment);

    return commentsMapper.toDto(savedComment);
  }
}

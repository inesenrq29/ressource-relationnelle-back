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
  public CommentDto addComment(UUID userId, UUID resourceId, CreateCommentDto createCommentDto) {
    // vérifie si l'utilisateur existe
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    // vérifie si la ressource existe
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));
    // vérifie que le contenu du commentaire n'est ni nul ni vide
    if (createCommentDto.getCommentsContent() == null
        || createCommentDto.getCommentsContent().isBlank()) {
      throw new BadRequestException("Comment content is required");
    }

    // création du commentaire
    final Comments comment = new Comments();

    comment.setTitleComments(createCommentDto.getTitleComments());
    comment.setCommentsContent(createCommentDto.getCommentsContent());
    comment.setResource(resource);
    comment.setAuthor(user.getPseudo());
    comment.setPublicationDate(Instant.now());
    comment.setStatus(CommentStatus.PENDING);

    // enregistrement en base
    final Comments savedComment = commentsRepository.save(comment);

    return commentsMapper.toDto(savedComment);
  }

  @Override
  public CommentDto respondToComment(
      UUID userId, UUID resourceId, UUID commentsId, CreateCommentDto createCommentDto) {
    // vérifie que le user existe
    final AppUser user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    // vérifie que la ressource existe
    final Resource resource =
        resourceRepository
            .findByResourceId(resourceId)
            .orElseThrow(() -> new NotFoundException("Resource not found"));
    // vérifie que le commentaire de base existe
    final Comments parentComment =
        commentsRepository
            .findByCommentsId(commentsId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
    // la ressource liée à id parent doit être = resource id
    if (!parentComment.getResource().getResourceId().equals(resourceId)) {
      throw new BadRequestException("Comment does not belong to this resource");
    }
    // vérifie que le commentaire n'est ni vide ni nul
    if (createCommentDto.getCommentsContent() == null
        || createCommentDto.getCommentsContent().isBlank()) {
      throw new BadRequestException("Comment content is required");
    }
    // création du commentaire de réponse
    final Comments response = new Comments();

    response.setParentComment(parentComment);
    response.setTitleComments(createCommentDto.getTitleComments());
    response.setCommentsContent(createCommentDto.getCommentsContent());
    response.setResource(resource);
    response.setAuthor(user.getPseudo());
    response.setPublicationDate(Instant.now());
    response.setStatus(CommentStatus.PENDING);

    // enregistrement en base
    final Comments savedResponse = commentsRepository.save(response);

    return commentsMapper.toDto(savedResponse);
  }

  @Override
  @Transactional
  public CommentDto moderateComment(UUID commentsId, ModerateCommentDto moderateCommentDto) {
    // vérifie que le commentaire existe
    final Comments comment =
        commentsRepository
            .findByCommentsId(commentsId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));

    // vérifie que le statut est défini
    if (moderateCommentDto.getStatus() == null) {
      throw new BadRequestException("Comment status is required");
    }

    // vérifie que le statut n'est pas PENDING
    if (moderateCommentDto.getStatus() == CommentStatus.PENDING) {
      throw new BadRequestException("Invalid moderation status");
    }

    // vérifie que si le statut est REJECTED la raison soit renseignée
    if (moderateCommentDto.getStatus() == CommentStatus.REJECTED) {
      if (moderateCommentDto.getModerationReason() == null
          || moderateCommentDto.getModerationReason().isBlank()) {
        throw new BadRequestException("Moderation reason is required");
      }
      // ajout de la raison de la modération
      comment.setModerationReason(moderateCommentDto.getModerationReason());
    } else {
      // sinon le statut est APPROVED et donc pas de justification
      comment.setModerationReason(null);
    }
    comment.setStatus(moderateCommentDto.getStatus());

    return commentsMapper.toDto(comment);
  }
}

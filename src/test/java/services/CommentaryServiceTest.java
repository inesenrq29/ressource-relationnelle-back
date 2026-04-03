package services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.ienrique.ressourceRelationnelle.service.CommentaryServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CommentaryServiceTest {

  @Mock private CommentsRepository commentsRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private AppUserRepository userRepository;
  @Mock private CommentsMapper commentsMapper;

  @InjectMocks private CommentaryServiceImpl commentaryService;
  private UUID userId;
  private UUID resourceId;
  private UUID commentId;

  private AppUser user;
  private Resource resource;
  private Comments comment;
  private CommentDto commentDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    resourceId = UUID.randomUUID();
    commentId = UUID.randomUUID();

    user = new AppUser();
    user.setAppUserId(userId);
    user.setPseudo("ines");

    resource = new Resource();
    resource.setResourceId(resourceId);

    comment = new Comments();
    comment.setCommentsId(commentId);
    comment.setResource(resource);

    commentDto = new CommentDto();
    commentDto.setCommentsId(commentId);
    commentDto.setAuthor("ines");
    commentDto.setResourceId(resourceId);
    commentDto.setStatus(CommentStatus.PENDING);
  }

  @Nested
  @DisplayName("add comment")
  class AddComment {
    @Test
    @DisplayName("should add comment successfully")
    void shouldAddCommentSuccessfully() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setTitleComments("Titre");
      requestDto.setCommentsContent("Contenu du commentaire");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(commentsRepository.save(any(Comments.class))).thenReturn(comment);
      when(commentsMapper.toDto(comment)).thenReturn(commentDto);

      final CommentDto result = commentaryService.addComment(userId, resourceId, requestDto);

      assertNotNull(result);
      assertEquals(commentDto, result);

      final ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);
      verify(commentsRepository).save(captor.capture());

      final Comments savedComment = captor.getValue();
      assertEquals("Titre", savedComment.getTitleComments());
      assertEquals("Contenu du commentaire", savedComment.getCommentsContent());
      assertEquals(resource, savedComment.getResource());
      assertEquals("ines", savedComment.getAuthor());
      assertEquals(CommentStatus.PENDING, savedComment.getStatus());
      assertNotNull(savedComment.getPublicationDate());

      verify(commentsMapper).toDto(comment);
    }

    @Test
    @DisplayName("should throw when addComment user not found")
    void shouldThrowWhenAddCommentUserNotFound() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("Contenu");

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> commentaryService.addComment(userId, resourceId, requestDto));

      assertEquals("User not found", exception.getMessage());
      verify(resourceRepository, never()).findByResourceId(any());
      verify(commentsRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when addComment resource not found")
    void shouldThrowWhenAddCommentResourceNotFound() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("Contenu");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> commentaryService.addComment(userId, resourceId, requestDto));

      assertEquals("Resource not found", exception.getMessage());
      verify(commentsRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when addComment content is blank")
    void shouldThrowWhenAddCommentContentIsBlank() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("   ");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.addComment(userId, resourceId, requestDto));

      assertEquals("Comment content is required", exception.getMessage());
      verify(commentsRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("respond to comment")
  class RespondToComment {
    @Test
    @DisplayName("should respond to comment successfully")
    void shouldRespondToCommentSuccessfully() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setTitleComments("Réponse");
      requestDto.setCommentsContent("Contenu de la réponse");

      final Comments parentComment = new Comments();
      parentComment.setCommentsId(commentId);
      parentComment.setResource(resource);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(parentComment));
      when(commentsRepository.save(any(Comments.class))).thenReturn(comment);
      when(commentsMapper.toDto(comment)).thenReturn(commentDto);

      final CommentDto result =
          commentaryService.respondToComment(userId, resourceId, commentId, requestDto);

      assertNotNull(result);
      assertEquals(commentDto, result);

      final ArgumentCaptor<Comments> captor = ArgumentCaptor.forClass(Comments.class);
      verify(commentsRepository).save(captor.capture());

      final Comments savedResponse = captor.getValue();
      assertEquals(parentComment, savedResponse.getParentComment());
      assertEquals("Réponse", savedResponse.getTitleComments());
      assertEquals("Contenu de la réponse", savedResponse.getCommentsContent());
      assertEquals(resource, savedResponse.getResource());
      assertEquals("ines", savedResponse.getAuthor());
      assertEquals(CommentStatus.PENDING, savedResponse.getStatus());
      assertNotNull(savedResponse.getPublicationDate());
    }

    @Test
    @DisplayName("should throw when respondToComment parent comment not found")
    void shouldThrowWhenRespondToCommentParentCommentNotFound() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("Réponse");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> commentaryService.respondToComment(userId, resourceId, commentId, requestDto));

      assertEquals("Comment not found", exception.getMessage());
      verify(commentsRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when respondToComment parent comment does not belong to resource")
    void shouldThrowWhenRespondToCommentParentCommentDoesNotBelongToResource() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("Réponse");

      final Resource anotherResource = new Resource();
      anotherResource.setResourceId(UUID.randomUUID());

      final Comments parentComment = new Comments();
      parentComment.setCommentsId(commentId);
      parentComment.setResource(anotherResource);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(parentComment));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.respondToComment(userId, resourceId, commentId, requestDto));

      assertEquals("Comment does not belong to this resource", exception.getMessage());
      verify(commentsRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw when respondToComment content is blank")
    void shouldThrowWhenRespondToCommentContentIsBlank() {
      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setCommentsContent("   ");

      final Comments parentComment = new Comments();
      parentComment.setCommentsId(commentId);
      parentComment.setResource(resource);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(resourceRepository.findByResourceId(resourceId)).thenReturn(Optional.of(resource));
      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(parentComment));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.respondToComment(userId, resourceId, commentId, requestDto));

      assertEquals("Comment content is required", exception.getMessage());
      verify(commentsRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("moderate comment")
  class ModerateComment {
    @Test
    @DisplayName("should approve comment successfully")
    void shouldApproveCommentSuccessfully() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.APPROVED);
      requestDto.setModerationReason(null);

      final CommentDto approvedDto = new CommentDto();
      approvedDto.setCommentsId(commentId);
      approvedDto.setStatus(CommentStatus.APPROVED);

      final Comments existingComment = new Comments();
      existingComment.setCommentsId(commentId);
      existingComment.setStatus(CommentStatus.PENDING);
      existingComment.setModerationReason("ancienne raison");

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(existingComment));
      when(commentsMapper.toDto(existingComment)).thenReturn(approvedDto);

      final CommentDto result = commentaryService.moderateComment(commentId, requestDto);

      assertNotNull(result);
      assertEquals(CommentStatus.APPROVED, existingComment.getStatus());
      assertNull(existingComment.getModerationReason());
      assertEquals(approvedDto, result);
    }

    @Test
    @DisplayName("should reject comment successfully with reason")
    void shouldRejectCommentSuccessfullyWithReason() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.REJECTED);
      requestDto.setModerationReason("Contenu inapproprié");

      final CommentDto rejectedDto = new CommentDto();
      rejectedDto.setCommentsId(commentId);
      rejectedDto.setStatus(CommentStatus.REJECTED);

      final Comments existingComment = new Comments();
      existingComment.setCommentsId(commentId);
      existingComment.setStatus(CommentStatus.PENDING);

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(existingComment));
      when(commentsMapper.toDto(existingComment)).thenReturn(rejectedDto);

      final CommentDto result = commentaryService.moderateComment(commentId, requestDto);

      assertNotNull(result);
      assertEquals(CommentStatus.REJECTED, existingComment.getStatus());
      assertEquals("Contenu inapproprié", existingComment.getModerationReason());
      assertEquals(rejectedDto, result);
    }

    @Test
    @DisplayName("should throw when moderateComment comment not found")
    void shouldThrowWhenModerateCommentCommentNotFound() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.APPROVED);

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.empty());

      final NotFoundException exception =
          assertThrows(
              NotFoundException.class,
              () -> commentaryService.moderateComment(commentId, requestDto));

      assertEquals("Comment not found", exception.getMessage());
      verify(commentsMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw when moderateComment status is null")
    void shouldThrowWhenModerateCommentStatusIsNull() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(null);

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(comment));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.moderateComment(commentId, requestDto));

      assertEquals("Comment status is required", exception.getMessage());
      verify(commentsMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw when moderateComment status is pending")
    void shouldThrowWhenModerateCommentStatusIsPending() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.PENDING);

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(comment));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.moderateComment(commentId, requestDto));

      assertEquals("Invalid moderation status", exception.getMessage());
      verify(commentsMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("should throw when moderateComment rejected without reason")
    void shouldThrowWhenModerateCommentRejectedWithoutReason() {
      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.REJECTED);
      requestDto.setModerationReason("   ");

      when(commentsRepository.findByCommentsId(commentId)).thenReturn(Optional.of(comment));

      final BadRequestException exception =
          assertThrows(
              BadRequestException.class,
              () -> commentaryService.moderateComment(commentId, requestDto));

      assertEquals("Moderation reason is required", exception.getMessage());
      verify(commentsMapper, never()).toDto(any());
    }
  }
}

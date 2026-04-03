package controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ienrique.ressourceRelationnelle.RessourceRelationnelleApplication;
import com.ienrique.ressourceRelationnelle.controller.CommentaryController;
import com.ienrique.ressourceRelationnelle.dto.CommentDto;
import com.ienrique.ressourceRelationnelle.dto.CreateCommentDto;
import com.ienrique.ressourceRelationnelle.dto.ModerateCommentDto;
import com.ienrique.ressourceRelationnelle.entity.CommentStatus;
import com.ienrique.ressourceRelationnelle.service.CommentaryService;

@WebMvcTest(CommentaryController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class CommentaryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private CommentaryService commentaryService;

  @Nested
  @DisplayName("add comment")
  class AddComment {
    @Test
    @DisplayName("should add comment")
    void shouldAddComment() throws Exception {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();
      final UUID commentId = UUID.randomUUID();

      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setTitleComments("Titre commentaire");
      requestDto.setCommentsContent("Contenu du commentaire");

      final CommentDto responseDto = new CommentDto();
      responseDto.setCommentsId(commentId);
      responseDto.setPublicationDate(Instant.now());
      responseDto.setAuthor("ines");
      responseDto.setTitleComments("Titre commentaire");
      responseDto.setCommentsContent("Contenu du commentaire");
      responseDto.setResourceId(resourceId);
      responseDto.setStatus(CommentStatus.PENDING);

      when(commentaryService.addComment(eq(userId), eq(resourceId), any(CreateCommentDto.class)))
          .thenReturn(responseDto);

      mockMvc
          .perform(
              post("/api/comments/{userId}/resources/{resourceId}", userId, resourceId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isCreated());

      verify(commentaryService).addComment(eq(userId), eq(resourceId), any(CreateCommentDto.class));
    }
  }

  @Nested
  @DisplayName("respond to comment")
  class RespondToComment {
    @Test
    @DisplayName("should respond to comment")
    void shouldRespondToComment() throws Exception {
      final UUID userId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();
      final UUID parentCommentId = UUID.randomUUID();
      final UUID responseCommentId = UUID.randomUUID();

      final CreateCommentDto requestDto = new CreateCommentDto();
      requestDto.setTitleComments("Réponse");
      requestDto.setCommentsContent("Ceci est une réponse");

      final CommentDto responseDto = new CommentDto();
      responseDto.setCommentsId(responseCommentId);
      responseDto.setPublicationDate(Instant.now());
      responseDto.setAuthor("ines");
      responseDto.setTitleComments("Réponse");
      responseDto.setCommentsContent("Ceci est une réponse");
      responseDto.setResourceId(resourceId);
      responseDto.setStatus(CommentStatus.PENDING);

      when(commentaryService.respondToComment(
              eq(userId), eq(resourceId), eq(parentCommentId), any(CreateCommentDto.class)))
          .thenReturn(responseDto);

      mockMvc
          .perform(
              post(
                      "/api/comments/{userId}/resources/{resourceId}/{commentsId}/reply",
                      userId,
                      resourceId,
                      parentCommentId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isCreated());

      verify(commentaryService)
          .respondToComment(
              eq(userId), eq(resourceId), eq(parentCommentId), any(CreateCommentDto.class));
    }
  }

  @Nested
  @DisplayName("moderate comment")
  class ModerateComment {
    @Test
    @DisplayName("should moderate comment")
    void shouldModerateComment() throws Exception {
      final UUID commentId = UUID.randomUUID();
      final UUID resourceId = UUID.randomUUID();

      final ModerateCommentDto requestDto = new ModerateCommentDto();
      requestDto.setStatus(CommentStatus.APPROVED);
      requestDto.setModerationReason(null);

      final CommentDto responseDto = new CommentDto();
      responseDto.setCommentsId(commentId);
      responseDto.setPublicationDate(Instant.now());
      responseDto.setAuthor("ines");
      responseDto.setTitleComments("Commentaire modéré");
      responseDto.setCommentsContent("Contenu validé");
      responseDto.setResourceId(resourceId);
      responseDto.setStatus(CommentStatus.APPROVED);

      when(commentaryService.moderateComment(eq(commentId), any(ModerateCommentDto.class)))
          .thenReturn(responseDto);

      mockMvc
          .perform(
              patch("/api/comments/{commentsId}/moderate", commentId)
                  .with(jwt())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(requestDto)))
          .andExpect(status().isOk());

      verify(commentaryService).moderateComment(eq(commentId), any(ModerateCommentDto.class));
    }
  }
}

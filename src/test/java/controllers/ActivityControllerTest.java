package controllers;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
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
import com.ienrique.ressourceRelationnelle.controller.ActivityController;
import com.ienrique.ressourceRelationnelle.dto.ActivityMessageDto;
import com.ienrique.ressourceRelationnelle.dto.AnswerPollDto;
import com.ienrique.ressourceRelationnelle.dto.SendMessageDto;
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.entity.ActivityType;
import com.ienrique.ressourceRelationnelle.service.ActivityService;

@WebMvcTest(ActivityController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class ActivityControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ActivityService activityService;

  @Nested
  @DisplayName("check quiz answer")
  class CheckQuizAnswer {
    @Test
    void shouldCheckQuizAnswer() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();
      final CheckQuizAnswerDto answerDto = new CheckQuizAnswerDto();
      answerDto.setQuizQuestionId(UUID.randomUUID());
      answerDto.setUserAnswer(true);
      answerDto.setActivitySessionId(activitySessionId);

      final CheckQuizAnswerResponseDto response = new CheckQuizAnswerResponseDto();
      response.setCorrect(true);

      when(activityService.answerQuizQuestion(answerDto)).thenReturn(response);

      mockMvc
          .perform(
              post("/api/activity/quiz/answer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(answerDto)))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("start activity")
  class StartActivity {
    @Test
    void shouldStartActivity() throws Exception {
      final UUID resourceId = UUID.randomUUID();
      final ActivityResponseDto response = new ActivityResponseDto();
      response.setActivityType(ActivityType.QUIZ);

      when(activityService.startActivity(resourceId)).thenReturn(response);

      mockMvc
          .perform(
              post("/api/activity/start/{resourceId}", resourceId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(response))
                  .with(jwt()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("create quiz")
  class CreateQuiz {
    @Test
    void shouldCreateQuiz() throws Exception {
      final CreateQuizDto createQuizDto = new CreateQuizDto();

      final QuizDto response = new QuizDto();

      when(activityService.createQuiz(createQuizDto)).thenReturn(response);

      mockMvc
          .perform(
              post("/api/activity/quiz")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(response))
                  .with(jwt()))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("create poll")
  class CreatePoll {
    @Test
    @DisplayName("should create poll")
    void shouldCreatePoll() throws Exception {
      final UUID resourceId = UUID.randomUUID();
      final CreatePollDto createPollDto = new CreatePollDto();
      createPollDto.setResourceId(resourceId);
      createPollDto.setQuestion("Quelle option préférez-vous ?");
      createPollDto.setOptions(List.of("Option 1", "Option 2"));

      final PollDto response = new PollDto();
      response.setPollId(UUID.randomUUID());
      response.setResourceId(resourceId);
      response.setQuestion("Quelle option préférez-vous ?");
      response.setCreatedAt(Instant.now());

      when(activityService.createPoll(createPollDto)).thenReturn(response);

      mockMvc
          .perform(
              post("/api/activity/poll")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(createPollDto))
                  .with(jwt()))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("check poll answer")
  class CheckPollOptionAnswer {

    @Test
    @DisplayName("should check poll option answer")
    void shouldCheckPollOptionAnswer() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID pollOptionId = UUID.randomUUID();
      final AnswerPollDto request = new AnswerPollDto();
      request.setActivitySessionId(activitySessionId);
      request.setPollOptionId(pollOptionId);

      activityService.answerPollOption(request);

      mockMvc
          .perform(
              post("/api/activity/poll/answer")
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("get quiz score")
  class GetQuizScore {

    @Test
    @DisplayName("should get quiz score")
    void shouldGetQuizScore() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();

      when(activityService.getQuizScore(activitySessionId)).thenReturn(5);

      mockMvc
          .perform(
              get("/api/activity/quiz/score/{activitySessionId}", activitySessionId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("invite participant")
  class InviteParticipant {

    @Test
    @DisplayName("should invite participant")
    void shouldInviteParticipant() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      activityService.inviteParticipant(activitySessionId, friendId);

      mockMvc
          .perform(
              post(
                      "/api/activity/{activitySessionId}/participants/invite/{friendId}",
                      activitySessionId,
                      friendId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isNoContent());
    }
  }

  @Nested
  @DisplayName("acceptInvitation")
  class AcceptInvitation {

    @Test
    @DisplayName("should accept invitation")
    void shouldAcceptInvitation() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();

      activityService.acceptInvitation(activitySessionId);

      mockMvc
          .perform(
              post("/api/activity/{activitySessionId}/participants/accept", activitySessionId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("declineInvitation")
  class DeclineInvitation {

    @Test
    @DisplayName("should decline invitation")
    void shouldDeclineInvitation() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();

      activityService.declineInvitation(activitySessionId);

      mockMvc
          .perform(
              post("/api/activity/{activitySessionId}/participants/decline", activitySessionId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("sendMessage")
  class SendMessage {

    @Test
    @DisplayName("should send message")
    void shouldSendMessage() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();
      final SendMessageDto content = new SendMessageDto();
      content.setContent("Content");

      activityService.sendMessage(activitySessionId, content);

      mockMvc
          .perform(
              post("/api/activity/messages/{activitySessionId}", activitySessionId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt())
                  .content(objectMapper.writeValueAsString(content)))
          .andExpect(status().isCreated());
    }
  }

  @Nested
  @DisplayName("getMessages")
  class GetMessages {

    @Test
    @DisplayName("should get messages")
    void shouldGetMessages() throws Exception {
      final UUID activitySessionId = UUID.randomUUID();
      final ActivityMessageDto message1 = new ActivityMessageDto();
      final ActivityMessageDto message2 = new ActivityMessageDto();

      when(activityService.getMessages(activitySessionId)).thenReturn(List.of(message1, message2));

      mockMvc
          .perform(
              get("/api/activity/messages/{activitySessionId}", activitySessionId)
                  .contentType(MediaType.APPLICATION_JSON)
                  .with(jwt()))
          .andExpect(status().isOk());
    }
  }
}

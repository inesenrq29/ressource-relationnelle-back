package controllers;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.entity.ActivityType;
import com.ienrique.ressourceRelationnelle.service.ActivityService;

@WebMvcTest(ActivityController.class)
@ContextConfiguration(classes = RessourceRelationnelleApplication.class)
public class ActivityControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private ActivityService activityService;

  @Test
  void shouldCheckQuizAnswer() throws Exception {
    final CheckQuizAnswerDto answerDto = new CheckQuizAnswerDto();
    answerDto.setQuizQuestionId(UUID.randomUUID());
    answerDto.setUserAnswer(true);

    final CheckQuizAnswerResponseDto response = new CheckQuizAnswerResponseDto();
    response.setCorrect(true);

    when(activityService.checkQuizAnswer(answerDto)).thenReturn(response);

    mockMvc
        .perform(
            post("/api/activity/quiz/check")
                .contentType(MediaType.APPLICATION_JSON)
                .with(jwt())
                .content(objectMapper.writeValueAsString(answerDto)))
        .andExpect(status().isOk());
  }

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

  @Test
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

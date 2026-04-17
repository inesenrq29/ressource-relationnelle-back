package services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ienrique.ressourceRelationnelle.dto.ActivityMessageDto;
import com.ienrique.ressourceRelationnelle.dto.AnswerPollDto;
import com.ienrique.ressourceRelationnelle.dto.SendMessageDto;
import com.ienrique.ressourceRelationnelle.dto.UserDto;
import com.ienrique.ressourceRelationnelle.dto.activity.*;
import com.ienrique.ressourceRelationnelle.entity.*;
import com.ienrique.ressourceRelationnelle.exception.BadRequestException;
import com.ienrique.ressourceRelationnelle.exception.NotFoundException;
import com.ienrique.ressourceRelationnelle.mapper.PollMapper;
import com.ienrique.ressourceRelationnelle.mapper.QuizMapper;
import com.ienrique.ressourceRelationnelle.repository.*;
import com.ienrique.ressourceRelationnelle.service.ActivityServiceImpl;
import com.ienrique.ressourceRelationnelle.service.UserService;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

  @Mock private QuizRepository quizRepository;
  @Mock private AppUserRepository appUserRepository;
  @Mock private InteractiveResourceRepository interactiveResourceRepository;
  @Mock private ResourceRepository resourceRepository;
  @Mock private ActivitySessionRepository activitySessionRepository;
  @Mock private ActivityMessageRepository activityMessageRepository;
  @Mock private ActivityParticipantRepository activityParticipantRepository;
  @Mock private QuizQuestionRepository quizQuestionRepository;
  @Mock private QuizParticipantAnswerRepository quizParticipantAnswerRepository;
  @Mock private PollOptionRepository pollOptionRepository;
  @Mock private PollParticipantAnswerRepository pollParticipantAnswerRepository;
  @Mock private FriendRepository friendRepository;
  @Mock private PollRepository pollRepository;
  @Mock private QuizMapper quizMapper;
  @Mock private PollMapper pollMapper;
  @Mock private UserService userService;

  @InjectMocks private ActivityServiceImpl activityService;

  @Nested
  @DisplayName("start activity")
  class StartActivity {

    @Test
    @DisplayName("Should start quiz activity successfully")
    void shouldStartQuizActivity() {
      final UUID resourceId = UUID.randomUUID();
      final UUID interactiveResourceId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID activitySessionId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final InteractiveResource interactiveResource = new InteractiveResource();
      interactiveResource.setInteractiveResourceId(interactiveResourceId);
      interactiveResource.setActivityType(ActivityType.QUIZ);
      interactiveResource.setResource(resource);

      final Quiz quiz = new Quiz();
      quiz.setQuizId(UUID.randomUUID());
      quiz.setCreatedAt(Instant.now());
      quiz.setInteractiveResource(interactiveResource);
      quiz.setQuestions(List.of());

      final ActivitySession savedSession = new ActivitySession();
      savedSession.setActivitySessionId(activitySessionId);
      savedSession.setInteractiveResource(interactiveResource);
      savedSession.setCreatedBy(user);
      savedSession.setStartedAt(Instant.now());
      savedSession.setStatus(ActivitySessionStatus.ACTIVE);

      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.of(interactiveResource));

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

      when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(savedSession);

      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(quizRepository.findByInteractiveResourceInteractiveResourceId(interactiveResourceId))
          .thenReturn(Optional.of(quiz));

      final ActivityResponseDto result = activityService.startActivity(resourceId);

      assertNotNull(result);
      assertEquals(activitySessionId, result.getActivitySessionId());
      assertEquals(ActivityType.QUIZ, result.getActivityType());
      assertNotNull(result.getQuiz());
      assertNull(result.getPoll());

      verify(activitySessionRepository).save(any(ActivitySession.class));
      verify(activityParticipantRepository).save(any(ActivityParticipant.class));
      verify(quizRepository).findByInteractiveResourceInteractiveResourceId(interactiveResourceId);
      verifyNoInteractions(pollRepository);
    }

    @Test
    @DisplayName("Should start poll activity successfully")
    void shouldStartPollActivity() {
      final UUID resourceId = UUID.randomUUID();
      final UUID interactiveResourceId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID activitySessionId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final InteractiveResource interactiveResource = new InteractiveResource();
      interactiveResource.setInteractiveResourceId(interactiveResourceId);
      interactiveResource.setActivityType(ActivityType.POLL);
      interactiveResource.setResource(resource);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final Poll poll = new Poll();
      poll.setPollId(UUID.randomUUID());
      poll.setQuestion("Question test");
      poll.setCreatedAt(Instant.now());
      poll.setInteractiveResource(interactiveResource);
      poll.setOptions(List.of());

      final PollDto pollDto = new PollDto();
      pollDto.setPollId(poll.getPollId());
      pollDto.setResourceId(resourceId);
      pollDto.setQuestion(poll.getQuestion());
      pollDto.setCreatedAt(poll.getCreatedAt());
      pollDto.setOptions(List.of());

      final ActivitySession savedSession = new ActivitySession();
      savedSession.setActivitySessionId(activitySessionId);
      savedSession.setInteractiveResource(interactiveResource);
      savedSession.setCreatedBy(user);
      savedSession.setStartedAt(Instant.now());
      savedSession.setStatus(ActivitySessionStatus.ACTIVE);

      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.of(interactiveResource));

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));

      when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(savedSession);

      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      when(pollRepository.findByInteractiveResourceInteractiveResourceId(interactiveResourceId))
          .thenReturn(Optional.of(poll));

      when(pollMapper.toDto(poll)).thenReturn(pollDto);

      final ActivityResponseDto result = activityService.startActivity(resourceId);

      assertNotNull(result);
      assertEquals(activitySessionId, result.getActivitySessionId());
      assertEquals(ActivityType.POLL, result.getActivityType());
      assertNotNull(result.getPoll());
      assertEquals(pollDto, result.getPoll());
      assertNull(result.getQuiz());

      verify(activitySessionRepository).save(any(ActivitySession.class));
      verify(activityParticipantRepository).save(any(ActivityParticipant.class));
      verify(pollRepository).findByInteractiveResourceInteractiveResourceId(interactiveResourceId);
      verifyNoInteractions(quizRepository);
    }

    @Test
    @DisplayName("Should return response with null activity content when activity type is null")
    void shouldReturnResponseWhenActivityTypeIsNull() {
      final UUID resourceId = UUID.randomUUID();
      final UUID interactiveResourceId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();
      final UUID activitySessionId = UUID.randomUUID();

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);

      final InteractiveResource interactiveResource = new InteractiveResource();
      interactiveResource.setInteractiveResourceId(interactiveResourceId);
      interactiveResource.setActivityType(null);
      interactiveResource.setResource(resource);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivitySession savedSession = new ActivitySession();
      savedSession.setActivitySessionId(activitySessionId);
      savedSession.setInteractiveResource(interactiveResource);
      savedSession.setCreatedBy(user);
      savedSession.setStartedAt(Instant.now());
      savedSession.setStatus(ActivitySessionStatus.ACTIVE);

      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.of(interactiveResource));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(appUserRepository.findById(userId)).thenReturn(Optional.of(user));
      when(activitySessionRepository.save(any(ActivitySession.class))).thenReturn(savedSession);
      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      final ActivityResponseDto result = activityService.startActivity(resourceId);

      assertNotNull(result);
      assertEquals(activitySessionId, result.getActivitySessionId());
      assertNull(result.getActivityType());
      assertNull(result.getQuiz());
      assertNull(result.getPoll());

      verify(activitySessionRepository).save(any(ActivitySession.class));
      verify(activityParticipantRepository).save(any(ActivityParticipant.class));
      verifyNoInteractions(quizRepository, pollRepository);
    }

    @Test
    @DisplayName("Should throw when interactive resource is not found")
    void shouldThrowWhenInteractiveResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();

      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.startActivity(resourceId));

      verify(interactiveResourceRepository).findByResourceResourceId(resourceId);
      verifyNoInteractions(
          userService,
          appUserRepository,
          activitySessionRepository,
          activityParticipantRepository,
          quizRepository,
          pollRepository);
    }
  }

  @Nested
  @DisplayName("create poll")
  class CreatePoll {
    @Test
    @DisplayName("Should create poll successfully")
    void shouldCreatePoll() {
      final UUID resourceId = UUID.randomUUID();
      final UUID interactiveResourceId = UUID.randomUUID();
      final UUID pollId = UUID.randomUUID();

      final CreatePollDto requestDto = new CreatePollDto();
      requestDto.setResourceId(resourceId);
      requestDto.setQuestion("Quelle option préférez-vous ?");
      requestDto.setOptions(List.of("Option 1", "Option 2"));

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.ACTIVITY_GAME);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.empty());

      final InteractiveResource savedInteractiveResource = new InteractiveResource();
      savedInteractiveResource.setInteractiveResourceId(interactiveResourceId);
      savedInteractiveResource.setActivityType(ActivityType.POLL);
      savedInteractiveResource.setResource(resource);

      when(interactiveResourceRepository.save(any(InteractiveResource.class)))
          .thenReturn(savedInteractiveResource);

      final Poll savedPoll = new Poll();
      savedPoll.setPollId(pollId);
      savedPoll.setQuestion(requestDto.getQuestion());
      savedPoll.setCreatedAt(Instant.now());
      savedPoll.setInteractiveResource(savedInteractiveResource);

      final List<PollOption> savedOptions =
          requestDto.getOptions().stream()
              .map(
                  optionLabel -> {
                    final PollOption option = new PollOption();
                    option.setOptionLabel(optionLabel);
                    option.setPoll(savedPoll);
                    return option;
                  })
              .toList();

      savedPoll.setOptions(savedOptions);

      when(pollRepository.save(any(Poll.class))).thenReturn(savedPoll);

      final PollDto pollDto = new PollDto();
      pollDto.setPollId(pollId);
      pollDto.setResourceId(resourceId);
      pollDto.setQuestion(requestDto.getQuestion());
      pollDto.setCreatedAt(savedPoll.getCreatedAt());
      pollDto.setOptions(
          requestDto.getOptions().stream()
              .map(
                  optionLabel -> {
                    final PollOptionDto dto = new PollOptionDto();
                    dto.setOptionLabel(optionLabel);
                    return dto;
                  })
              .toList());

      when(pollMapper.toDto(savedPoll)).thenReturn(pollDto);

      final PollDto result = activityService.createPoll(requestDto);

      assertEquals(pollId, result.getPollId());
      assertEquals(resourceId, result.getResourceId());
      assertEquals("Quelle option préférez-vous ?", result.getQuestion());
      assertNotNull(result.getOptions());
      assertEquals(2, result.getOptions().size());
      assertEquals("Option 1", result.getOptions().get(0).getOptionLabel());
      assertEquals("Option 2", result.getOptions().get(1).getOptionLabel());
    }

    @Test
    @DisplayName("Should throw when resource is not found")
    void shouldThrowWhenResourceNotFound() {
      final UUID resourceId = UUID.randomUUID();

      final CreatePollDto requestDto = new CreatePollDto();
      requestDto.setResourceId(resourceId);
      requestDto.setQuestion("Quelle option préférez-vous ?");
      requestDto.setOptions(List.of("Option 1", "Option 2"));

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.createPoll(requestDto));
    }

    @Test
    @DisplayName("Should throw when resource is not an activity game")
    void shouldThrowWhenResourceIsNotActivityGame() {
      final UUID resourceId = UUID.randomUUID();

      final CreatePollDto requestDto = new CreatePollDto();
      requestDto.setResourceId(resourceId);
      requestDto.setQuestion("Quelle option préférez-vous ?");
      requestDto.setOptions(List.of("Option 1", "Option 2"));

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.READING_SHEET);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

      assertThrows(BadRequestException.class, () -> activityService.createPoll(requestDto));
    }

    @Test
    @DisplayName("Should throw when interactive resource already exists")
    void shouldThrowWhenInteractiveResourceAlreadyExists() {
      final UUID resourceId = UUID.randomUUID();

      final CreatePollDto requestDto = new CreatePollDto();
      requestDto.setResourceId(resourceId);
      requestDto.setQuestion("Quelle option préférez-vous ?");
      requestDto.setOptions(List.of("Option 1", "Option 2"));

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.ACTIVITY_GAME);

      final InteractiveResource interactiveResource = new InteractiveResource();
      interactiveResource.setInteractiveResourceId(UUID.randomUUID());
      interactiveResource.setActivityType(ActivityType.POLL);
      interactiveResource.setResource(resource);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.of(interactiveResource));

      assertThrows(BadRequestException.class, () -> activityService.createPoll(requestDto));
    }
  }

  @Nested
  @DisplayName("create quiz")
  class CreateQuiz {
    @Test
    @DisplayName("Should create quiz successfully")
    void shouldCreateQuiz() {
      final UUID resourceId = UUID.randomUUID();
      final UUID interactiveResourceId = UUID.randomUUID();
      final UUID quizId = UUID.randomUUID();

      final CreateQuizDto requestDto = new CreateQuizDto();
      requestDto.setResourceId(resourceId);

      final QuizQuestionDto question1 = new QuizQuestionDto();
      question1.setQuestion("La communication est importante ?");
      question1.setCorrectAnswer(true);

      final QuizQuestionDto question2 = new QuizQuestionDto();
      question2.setQuestion("Ignorer l'autre améliore la relation ?");
      question2.setCorrectAnswer(false);

      requestDto.setQuestions(List.of(question1, question2));

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.ACTIVITY_GAME);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.empty());

      final InteractiveResource savedInteractiveResource = new InteractiveResource();
      savedInteractiveResource.setInteractiveResourceId(interactiveResourceId);
      savedInteractiveResource.setActivityType(ActivityType.QUIZ);
      savedInteractiveResource.setResource(resource);

      when(interactiveResourceRepository.save(any(InteractiveResource.class)))
          .thenReturn(savedInteractiveResource);

      final Quiz savedQuiz = new Quiz();
      savedQuiz.setQuizId(quizId);
      savedQuiz.setCreatedAt(Instant.now());
      savedQuiz.setInteractiveResource(savedInteractiveResource);

      final List<QuizQuestion> savedQuestions =
          requestDto.getQuestions().stream()
              .map(
                  q -> {
                    final QuizQuestion question = new QuizQuestion();
                    question.setQuestion(q.getQuestion());
                    question.setCorrectAnswer(q.isCorrectAnswer());
                    question.setQuiz(savedQuiz);
                    return question;
                  })
              .toList();

      savedQuiz.setQuestions(savedQuestions);

      when(quizRepository.save(any(Quiz.class))).thenReturn(savedQuiz);

      final QuizDto quizDto = new QuizDto();
      quizDto.setQuizId(quizId);
      quizDto.setResourceId(resourceId);
      quizDto.setCreatedAt(savedQuiz.getCreatedAt());
      quizDto.setQuestions(List.of(question1, question2));

      when(quizMapper.toDto(savedQuiz)).thenReturn(quizDto);

      final QuizDto result = activityService.createQuiz(requestDto);

      assertEquals(quizId, result.getQuizId());
      assertEquals(resourceId, result.getResourceId());
      assertNotNull(result.getQuestions());
      assertEquals(2, result.getQuestions().size());
      assertEquals("La communication est importante ?", result.getQuestions().get(0).getQuestion());
      assertTrue(result.getQuestions().get(0).isCorrectAnswer());
      assertEquals(
          "Ignorer l'autre améliore la relation ?", result.getQuestions().get(1).getQuestion());
      assertFalse(result.getQuestions().get(1).isCorrectAnswer());
    }

    @Test
    @DisplayName("Should throw when resource is not found for quiz creation")
    void shouldThrowWhenResourceNotFoundForQuizCreation() {
      final UUID resourceId = UUID.randomUUID();

      final CreateQuizDto requestDto = new CreateQuizDto();
      requestDto.setResourceId(resourceId);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.createQuiz(requestDto));
    }

    @Test
    @DisplayName("Should throw when resource is not an activity game for quiz creation")
    void shouldThrowWhenResourceIsNotActivityGameForQuizCreation() {
      final UUID resourceId = UUID.randomUUID();

      final CreateQuizDto requestDto = new CreateQuizDto();
      requestDto.setResourceId(resourceId);

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.READING_SHEET);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));

      assertThrows(BadRequestException.class, () -> activityService.createQuiz(requestDto));
    }

    @Test
    @DisplayName("Should throw when interactive resource already exists for quiz creation")
    void shouldThrowWhenInteractiveResourceAlreadyExistsForQuizCreation() {
      final UUID resourceId = UUID.randomUUID();

      final CreateQuizDto requestDto = new CreateQuizDto();
      requestDto.setResourceId(resourceId);

      final Resource resource = new Resource();
      resource.setResourceId(resourceId);
      resource.setResourceType(ResourceType.ACTIVITY_GAME);

      final InteractiveResource interactiveResource = new InteractiveResource();
      interactiveResource.setInteractiveResourceId(UUID.randomUUID());
      interactiveResource.setActivityType(ActivityType.QUIZ);
      interactiveResource.setResource(resource);

      when(resourceRepository.findById(resourceId)).thenReturn(Optional.of(resource));
      when(interactiveResourceRepository.findByResourceResourceId(resourceId))
          .thenReturn(Optional.of(interactiveResource));

      assertThrows(BadRequestException.class, () -> activityService.createQuiz(requestDto));
    }
  }

  @Nested
  @DisplayName("check quiz answer")
  class CheckQuizAnswer {

    @Test
    @DisplayName("Should return true when quiz answer is correct")
    void shouldReturnTrueWhenQuizAnswerIsCorrect() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID quizQuestionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setQuizQuestionId(quizQuestionId);
      requestDto.setUserAnswer(true);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final QuizQuestion question = new QuizQuestion();
      question.setQuizQuestionId(quizQuestionId);
      question.setQuestion("La communication est importante ?");
      question.setCorrectAnswer(true);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizQuestionRepository.findById(quizQuestionId)).thenReturn(Optional.of(question));
      when(quizParticipantAnswerRepository.existsByActivityParticipantAndQuizQuestion(
              participant, question))
          .thenReturn(false);
      when(quizParticipantAnswerRepository.save(any(QuizParticipantAnswer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      final CheckQuizAnswerResponseDto result = activityService.answerQuizQuestion(requestDto);

      assertNotNull(result);
      assertTrue(result.isCorrect());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizQuestionRepository).findById(quizQuestionId);
      verify(quizParticipantAnswerRepository)
          .existsByActivityParticipantAndQuizQuestion(participant, question);
      verify(quizParticipantAnswerRepository).save(any(QuizParticipantAnswer.class));
      verifyNoInteractions(quizRepository);
    }

    @Test
    @DisplayName("Should return false when quiz answer is incorrect")
    void shouldReturnFalseWhenQuizAnswerIsIncorrect() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID quizQuestionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setQuizQuestionId(quizQuestionId);
      requestDto.setUserAnswer(false);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final QuizQuestion question = new QuizQuestion();
      question.setQuizQuestionId(quizQuestionId);
      question.setQuestion("La communication est importante ?");
      question.setCorrectAnswer(true);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizQuestionRepository.findById(quizQuestionId)).thenReturn(Optional.of(question));
      when(quizParticipantAnswerRepository.existsByActivityParticipantAndQuizQuestion(
              participant, question))
          .thenReturn(false);
      when(quizParticipantAnswerRepository.save(any(QuizParticipantAnswer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      final CheckQuizAnswerResponseDto result = activityService.answerQuizQuestion(requestDto);

      assertNotNull(result);
      assertFalse(result.isCorrect());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizQuestionRepository).findById(quizQuestionId);
      verify(quizParticipantAnswerRepository)
          .existsByActivityParticipantAndQuizQuestion(participant, question);
      verify(quizParticipantAnswerRepository).save(any(QuizParticipantAnswer.class));
      verifyNoInteractions(quizRepository);
    }

    @Test
    @DisplayName("Should throw when user is not participant of activity session")
    void shouldThrowWhenUserIsNotParticipantOfActivitySession() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID quizQuestionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setQuizQuestionId(quizQuestionId);
      requestDto.setUserAnswer(true);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.answerQuizQuestion(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(quizQuestionRepository, quizParticipantAnswerRepository, quizRepository);
    }

    @Test
    @DisplayName("Should throw when question is not found")
    void shouldThrowWhenQuestionIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID quizQuestionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setQuizQuestionId(quizQuestionId);
      requestDto.setUserAnswer(true);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizQuestionRepository.findById(quizQuestionId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.answerQuizQuestion(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizQuestionRepository).findById(quizQuestionId);
      verifyNoInteractions(quizParticipantAnswerRepository, quizRepository);
    }

    @Test
    @DisplayName("Should throw when question already answered")
    void shouldThrowWhenQuestionAlreadyAnswered() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID quizQuestionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final CheckQuizAnswerDto requestDto = new CheckQuizAnswerDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setQuizQuestionId(quizQuestionId);
      requestDto.setUserAnswer(true);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final QuizQuestion question = new QuizQuestion();
      question.setQuizQuestionId(quizQuestionId);
      question.setQuestion("La communication est importante ?");
      question.setCorrectAnswer(true);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizQuestionRepository.findById(quizQuestionId)).thenReturn(Optional.of(question));
      when(quizParticipantAnswerRepository.existsByActivityParticipantAndQuizQuestion(
              participant, question))
          .thenReturn(true);

      assertThrows(BadRequestException.class, () -> activityService.answerQuizQuestion(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizQuestionRepository).findById(quizQuestionId);
      verify(quizParticipantAnswerRepository)
          .existsByActivityParticipantAndQuizQuestion(participant, question);
      verify(quizParticipantAnswerRepository, never()).save(any(QuizParticipantAnswer.class));
      verifyNoInteractions(quizRepository);
    }
  }

  @Nested
  @DisplayName("answer poll option")
  class AnswerPollOption {

    @Test
    @DisplayName("Should save poll answer successfully")
    void shouldSavePollAnswerSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID pollOptionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AnswerPollDto requestDto = new AnswerPollDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setPollOptionId(pollOptionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final Poll poll = new Poll();
      poll.setPollId(UUID.randomUUID());

      final PollOption pollOption = new PollOption();
      pollOption.setPollOptionId(pollOptionId);
      pollOption.setPoll(poll);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(pollOptionRepository.findById(pollOptionId)).thenReturn(Optional.of(pollOption));
      when(pollParticipantAnswerRepository.existsByActivityParticipantAndPollOptionPoll(
              participant, poll))
          .thenReturn(false);
      when(pollParticipantAnswerRepository.save(any(PollParticipantAnswer.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      activityService.answerPollOption(requestDto);

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(pollOptionRepository).findById(pollOptionId);
      verify(pollParticipantAnswerRepository)
          .existsByActivityParticipantAndPollOptionPoll(participant, poll);
      verify(pollParticipantAnswerRepository).save(any(PollParticipantAnswer.class));
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID pollOptionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AnswerPollDto requestDto = new AnswerPollDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setPollOptionId(pollOptionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.answerPollOption(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(pollOptionRepository, pollParticipantAnswerRepository);
    }

    @Test
    @DisplayName("Should throw when poll option is not found")
    void shouldThrowWhenPollOptionIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID pollOptionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AnswerPollDto requestDto = new AnswerPollDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setPollOptionId(pollOptionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(pollOptionRepository.findById(pollOptionId)).thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.answerPollOption(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(pollOptionRepository).findById(pollOptionId);
      verifyNoInteractions(pollParticipantAnswerRepository);
    }

    @Test
    @DisplayName("Should throw when poll already answered")
    void shouldThrowWhenPollAlreadyAnswered() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID pollOptionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final AnswerPollDto requestDto = new AnswerPollDto();
      requestDto.setActivitySessionId(activitySessionId);
      requestDto.setPollOptionId(pollOptionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final Poll poll = new Poll();
      poll.setPollId(UUID.randomUUID());

      final PollOption pollOption = new PollOption();
      pollOption.setPollOptionId(pollOptionId);
      pollOption.setPoll(poll);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(pollOptionRepository.findById(pollOptionId)).thenReturn(Optional.of(pollOption));
      when(pollParticipantAnswerRepository.existsByActivityParticipantAndPollOptionPoll(
              participant, poll))
          .thenReturn(true);

      assertThrows(BadRequestException.class, () -> activityService.answerPollOption(requestDto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(pollOptionRepository).findById(pollOptionId);
      verify(pollParticipantAnswerRepository)
          .existsByActivityParticipantAndPollOptionPoll(participant, poll);
      verify(pollParticipantAnswerRepository, never()).save(any(PollParticipantAnswer.class));
    }
  }

  @Nested
  @DisplayName("get quiz score")
  class GetQuizScore {

    @Test
    @DisplayName("Should return quiz score when participant has answers")
    void shouldReturnQuizScoreWhenParticipantHasAnswers() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final QuizQuestion question1 = new QuizQuestion();
      question1.setQuizQuestionId(UUID.randomUUID());
      question1.setCorrectAnswer(true);

      final QuizQuestion question2 = new QuizQuestion();
      question2.setQuizQuestionId(UUID.randomUUID());
      question2.setCorrectAnswer(false);

      final QuizQuestion question3 = new QuizQuestion();
      question3.setQuizQuestionId(UUID.randomUUID());
      question3.setCorrectAnswer(true);

      final QuizParticipantAnswer answer1 = new QuizParticipantAnswer();
      answer1.setActivityParticipant(participant);
      answer1.setQuizQuestion(question1);
      answer1.setUserAnswer(true); // correct

      final QuizParticipantAnswer answer2 = new QuizParticipantAnswer();
      answer2.setActivityParticipant(participant);
      answer2.setQuizQuestion(question2);
      answer2.setUserAnswer(true); // incorrect

      final QuizParticipantAnswer answer3 = new QuizParticipantAnswer();
      answer3.setActivityParticipant(participant);
      answer3.setQuizQuestion(question3);
      answer3.setUserAnswer(true); // correct

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizParticipantAnswerRepository.findByActivityParticipant(participant))
          .thenReturn(List.of(answer1, answer2, answer3));

      final int result = activityService.getQuizScore(activitySessionId);

      assertEquals(2, result);

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizParticipantAnswerRepository).findByActivityParticipant(participant);
    }

    @Test
    @DisplayName("Should return zero when participant has no answers")
    void shouldReturnZeroWhenParticipantHasNoAnswers() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizParticipantAnswerRepository.findByActivityParticipant(participant))
          .thenReturn(List.of());

      final int result = activityService.getQuizScore(activitySessionId);

      assertEquals(0, result);

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizParticipantAnswerRepository).findByActivityParticipant(participant);
    }

    @Test
    @DisplayName("Should return zero when all answers are incorrect")
    void shouldReturnZeroWhenAllAnswersAreIncorrect() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);

      final QuizQuestion question1 = new QuizQuestion();
      question1.setQuizQuestionId(UUID.randomUUID());
      question1.setCorrectAnswer(true);

      final QuizQuestion question2 = new QuizQuestion();
      question2.setQuizQuestionId(UUID.randomUUID());
      question2.setCorrectAnswer(false);

      final QuizParticipantAnswer answer1 = new QuizParticipantAnswer();
      answer1.setActivityParticipant(participant);
      answer1.setQuizQuestion(question1);
      answer1.setUserAnswer(false); // incorrect

      final QuizParticipantAnswer answer2 = new QuizParticipantAnswer();
      answer2.setActivityParticipant(participant);
      answer2.setQuizQuestion(question2);
      answer2.setUserAnswer(true); // incorrect

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(quizParticipantAnswerRepository.findByActivityParticipant(participant))
          .thenReturn(List.of(answer1, answer2));

      final int result = activityService.getQuizScore(activitySessionId);

      assertEquals(0, result);

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(quizParticipantAnswerRepository).findByActivityParticipant(participant);
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.getQuizScore(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(quizParticipantAnswerRepository);
    }
  }

  @Nested
  @DisplayName("invite participant")
  class InviteParticipant {

    @Test
    @DisplayName("Should invite participant successfully")
    void shouldInviteParticipantSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final AppUser friend = new AppUser();
      friend.setAppUserId(friendId);

      final ActivityParticipant hostParticipant = new ActivityParticipant();
      hostParticipant.setActivitySession(session);
      hostParticipant.setRole(ParticipantRole.HOST);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(hostParticipant));
      when(appUserRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(friendRepository.existsByRequesterUserAppUserIdAndReceiverUserAppUserId(
              currentUserId, friendId))
          .thenReturn(true);
      when(activityParticipantRepository.existsByActivitySessionAndAppUser(session, friend))
          .thenReturn(false);
      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      activityService.inviteParticipant(activitySessionId, friendId);

      final ArgumentCaptor<ActivityParticipant> captor =
          ArgumentCaptor.forClass(ActivityParticipant.class);

      verify(activityParticipantRepository).save(captor.capture());

      final ActivityParticipant savedParticipant = captor.getValue();
      assertEquals(session, savedParticipant.getActivitySession());
      assertEquals(friend, savedParticipant.getAppUser());
      assertEquals(ParticipantRole.PARTICIPANT, savedParticipant.getRole());
      assertEquals(ParticipantStatus.PENDING, savedParticipant.getStatus());
      assertNotNull(savedParticipant.getInvitedAt());
    }

    @Test
    @DisplayName("Should throw when session is not found")
    void shouldThrowWhenSessionIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(activitySessionRepository).findById(activitySessionId);
      verifyNoInteractions(
          userService, activityParticipantRepository, appUserRepository, friendRepository);
    }

    @Test
    @DisplayName("Should throw when current user is not participant")
    void shouldThrowWhenCurrentUserIsNotParticipant() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(activitySessionRepository).findById(activitySessionId);
      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId);
      verifyNoInteractions(appUserRepository, friendRepository);
    }

    @Test
    @DisplayName("Should throw when current user is not host")
    void shouldThrowWhenCurrentUserIsNotHost() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setActivitySession(session);
      participant.setRole(ParticipantRole.PARTICIPANT);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(participant));

      assertThrows(
          BadRequestException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(activitySessionRepository).findById(activitySessionId);
      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId);
      verifyNoInteractions(appUserRepository, friendRepository);
    }

    @Test
    @DisplayName("Should throw when friend is not found")
    void shouldThrowWhenFriendIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final ActivityParticipant hostParticipant = new ActivityParticipant();
      hostParticipant.setActivitySession(session);
      hostParticipant.setRole(ParticipantRole.HOST);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(hostParticipant));
      when(appUserRepository.findById(friendId)).thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(appUserRepository).findById(friendId);
      verifyNoInteractions(friendRepository);
    }

    @Test
    @DisplayName("Should throw when user invites himself")
    void shouldThrowWhenUserInvitesHimself() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final AppUser friend = new AppUser();
      friend.setAppUserId(currentUserId);

      final ActivityParticipant hostParticipant = new ActivityParticipant();
      hostParticipant.setActivitySession(session);
      hostParticipant.setRole(ParticipantRole.HOST);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(hostParticipant));
      when(appUserRepository.findById(currentUserId)).thenReturn(Optional.of(friend));

      assertThrows(
          BadRequestException.class,
          () -> activityService.inviteParticipant(activitySessionId, currentUserId));

      verify(appUserRepository).findById(currentUserId);
      verifyNoInteractions(friendRepository);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }

    @Test
    @DisplayName("Should throw when invited user is not a friend")
    void shouldThrowWhenInvitedUserIsNotFriend() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final AppUser friend = new AppUser();
      friend.setAppUserId(friendId);

      final ActivityParticipant hostParticipant = new ActivityParticipant();
      hostParticipant.setActivitySession(session);
      hostParticipant.setRole(ParticipantRole.HOST);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(hostParticipant));
      when(appUserRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(friendRepository.existsByRequesterUserAppUserIdAndReceiverUserAppUserId(
              currentUserId, friendId))
          .thenReturn(false);

      assertThrows(
          BadRequestException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(friendRepository)
          .existsByRequesterUserAppUserIdAndReceiverUserAppUserId(currentUserId, friendId);
      verify(activityParticipantRepository, never())
          .existsByActivitySessionAndAppUser(any(), any());
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }

    @Test
    @DisplayName("Should throw when friend is already participant")
    void shouldThrowWhenFriendIsAlreadyParticipant() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID currentUserId = UUID.randomUUID();
      final UUID friendId = UUID.randomUUID();

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(currentUserId);

      final AppUser friend = new AppUser();
      friend.setAppUserId(friendId);

      final ActivityParticipant hostParticipant = new ActivityParticipant();
      hostParticipant.setActivitySession(session);
      hostParticipant.setRole(ParticipantRole.HOST);

      when(activitySessionRepository.findById(activitySessionId)).thenReturn(Optional.of(session));
      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, currentUserId))
          .thenReturn(Optional.of(hostParticipant));
      when(appUserRepository.findById(friendId)).thenReturn(Optional.of(friend));
      when(friendRepository.existsByRequesterUserAppUserIdAndReceiverUserAppUserId(
              currentUserId, friendId))
          .thenReturn(true);
      when(activityParticipantRepository.existsByActivitySessionAndAppUser(session, friend))
          .thenReturn(true);

      assertThrows(
          BadRequestException.class,
          () -> activityService.inviteParticipant(activitySessionId, friendId));

      verify(activityParticipantRepository).existsByActivitySessionAndAppUser(session, friend);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }
  }

  @Nested
  @DisplayName("accept invitation")
  class AcceptInvitation {

    @Test
    @DisplayName("Should accept invitation successfully")
    void shouldAcceptInvitationSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setStatus(ParticipantStatus.PENDING);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      activityService.acceptInvitation(activitySessionId);

      assertEquals(ParticipantStatus.JOINED, participant.getStatus());
      assertNotNull(participant.getJoinedAt());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository).save(participant);
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> activityService.acceptInvitation(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }

    @Test
    @DisplayName("Should throw when invitation is not pending")
    void shouldThrowWhenInvitationIsNotPending() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setStatus(ParticipantStatus.JOINED);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));

      assertThrows(
          BadRequestException.class, () -> activityService.acceptInvitation(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }
  }

  @Nested
  @DisplayName("decline invitation")
  class DeclineInvitation {

    @Test
    @DisplayName("Should decline invitation successfully")
    void shouldDeclineInvitationSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setStatus(ParticipantStatus.PENDING);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(activityParticipantRepository.save(any(ActivityParticipant.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      activityService.declineInvitation(activitySessionId);

      assertEquals(ParticipantStatus.DECLINED, participant.getStatus());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository).save(participant);
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> activityService.declineInvitation(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }

    @Test
    @DisplayName("Should throw when invitation is not pending")
    void shouldThrowWhenInvitationIsNotPending() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setStatus(ParticipantStatus.JOINED);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));

      assertThrows(
          BadRequestException.class, () -> activityService.declineInvitation(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityParticipantRepository, never()).save(any(ActivityParticipant.class));
    }
  }

  @Nested
  @DisplayName("send message")
  class SendMessage {

    @Test
    @DisplayName("Should send message successfully")
    void shouldSendMessageSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final SendMessageDto dto = new SendMessageDto();
      dto.setContent("Hello world");

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivitySession session = new ActivitySession();
      session.setActivitySessionId(activitySessionId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setActivitySession(session);
      participant.setStatus(ParticipantStatus.JOINED);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(activityMessageRepository.save(any(ActivityMessage.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      activityService.sendMessage(activitySessionId, dto);

      final ArgumentCaptor<ActivityMessage> captor = ArgumentCaptor.forClass(ActivityMessage.class);

      verify(activityMessageRepository).save(captor.capture());

      final ActivityMessage savedMessage = captor.getValue();
      assertEquals(session, savedMessage.getActivitySession());
      assertEquals("Hello world", savedMessage.getContent());
      assertEquals(user, savedMessage.getSender());
      assertNotNull(savedMessage.getSentAt());
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final SendMessageDto dto = new SendMessageDto();
      dto.setContent("Hello");

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(
          NotFoundException.class, () -> activityService.sendMessage(activitySessionId, dto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(activityMessageRepository);
    }

    @Test
    @DisplayName("Should throw when participant is not joined")
    void shouldThrowWhenParticipantIsNotJoined() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final SendMessageDto dto = new SendMessageDto();
      dto.setContent("Hello");

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser user = new AppUser();
      user.setAppUserId(userId);

      final ActivitySession session = new ActivitySession();

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(user);
      participant.setActivitySession(session);
      participant.setStatus(ParticipantStatus.PENDING);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));

      assertThrows(
          BadRequestException.class, () -> activityService.sendMessage(activitySessionId, dto));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityMessageRepository, never()).save(any(ActivityMessage.class));
    }
  }

  @Nested
  @DisplayName("get messages")
  class GetMessages {

    @Test
    @DisplayName("Should return activity messages successfully")
    void shouldReturnActivityMessagesSuccessfully() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser currentAppUser = new AppUser();
      currentAppUser.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(currentAppUser);
      participant.setStatus(ParticipantStatus.JOINED);

      final AppUser sender1 = new AppUser();
      sender1.setAppUserId(UUID.randomUUID());
      sender1.setPseudo("alice");

      final AppUser sender2 = new AppUser();
      sender2.setAppUserId(UUID.randomUUID());
      sender2.setPseudo("bob");

      final ActivityMessage message1 = new ActivityMessage();
      message1.setActivityMessageId(UUID.randomUUID());
      message1.setSender(sender1);
      message1.setContent("Bonjour");
      message1.setSentAt(Instant.now().minusSeconds(60));

      final ActivityMessage message2 = new ActivityMessage();
      message2.setActivityMessageId(UUID.randomUUID());
      message2.setSender(sender2);
      message2.setContent("Salut");
      message2.setSentAt(Instant.now());

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(activityMessageRepository.findByActivitySessionActivitySessionIdOrderBySentAtAsc(
              activitySessionId))
          .thenReturn(List.of(message1, message2));

      final List<ActivityMessageDto> result = activityService.getMessages(activitySessionId);

      assertNotNull(result);
      assertEquals(2, result.size());

      assertEquals(message1.getActivityMessageId(), result.get(0).getActivityMessageId());
      assertEquals(sender1.getAppUserId(), result.get(0).getSenderId());
      assertEquals("alice", result.get(0).getSenderUsername());
      assertEquals("Bonjour", result.get(0).getContent());
      assertEquals(message1.getSentAt(), result.get(0).getSentAt());

      assertEquals(message2.getActivityMessageId(), result.get(1).getActivityMessageId());
      assertEquals(sender2.getAppUserId(), result.get(1).getSenderId());
      assertEquals("bob", result.get(1).getSenderUsername());
      assertEquals("Salut", result.get(1).getContent());
      assertEquals(message2.getSentAt(), result.get(1).getSentAt());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityMessageRepository)
          .findByActivitySessionActivitySessionIdOrderBySentAtAsc(activitySessionId);
    }

    @Test
    @DisplayName("Should return empty list when session has no messages")
    void shouldReturnEmptyListWhenSessionHasNoMessages() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser currentAppUser = new AppUser();
      currentAppUser.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(currentAppUser);
      participant.setStatus(ParticipantStatus.JOINED);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));
      when(activityMessageRepository.findByActivitySessionActivitySessionIdOrderBySentAtAsc(
              activitySessionId))
          .thenReturn(List.of());

      final List<ActivityMessageDto> result = activityService.getMessages(activitySessionId);

      assertNotNull(result);
      assertTrue(result.isEmpty());

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verify(activityMessageRepository)
          .findByActivitySessionActivitySessionIdOrderBySentAtAsc(activitySessionId);
    }

    @Test
    @DisplayName("Should throw when participant is not found")
    void shouldThrowWhenParticipantIsNotFound() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.empty());

      assertThrows(NotFoundException.class, () -> activityService.getMessages(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(activityMessageRepository);
    }

    @Test
    @DisplayName("Should throw when participant is not joined")
    void shouldThrowWhenParticipantIsNotJoined() {
      final UUID activitySessionId = UUID.randomUUID();
      final UUID userId = UUID.randomUUID();

      final UserDto currentUser = new UserDto();
      currentUser.setAppUserId(userId);

      final AppUser currentAppUser = new AppUser();
      currentAppUser.setAppUserId(userId);

      final ActivityParticipant participant = new ActivityParticipant();
      participant.setAppUser(currentAppUser);
      participant.setStatus(ParticipantStatus.PENDING);

      when(userService.getCurrentUser()).thenReturn(currentUser);
      when(activityParticipantRepository.findByActivitySessionActivitySessionIdAndAppUserAppUserId(
              activitySessionId, userId))
          .thenReturn(Optional.of(participant));

      assertThrows(BadRequestException.class, () -> activityService.getMessages(activitySessionId));

      verify(userService).getCurrentUser();
      verify(activityParticipantRepository)
          .findByActivitySessionActivitySessionIdAndAppUserAppUserId(activitySessionId, userId);
      verifyNoInteractions(activityMessageRepository);
    }
  }
}
